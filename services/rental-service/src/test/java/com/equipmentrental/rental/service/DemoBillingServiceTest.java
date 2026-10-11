package com.equipmentrental.rental.service;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import com.equipmentrental.common.security.CurrentUserProvider;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class DemoBillingServiceTest {
    RentalOrderRepository orders=mock(RentalOrderRepository.class);
    QuotationRepository quotes=mock(QuotationRepository.class);
    DemoRentalAccountRepository accounts=mock(DemoRentalAccountRepository.class);
    DemoRentalEventRepository events=mock(DemoRentalEventRepository.class);
    RentalDataScopeGuard scope=mock(RentalDataScopeGuard.class);
    CurrentUserProvider users=mock(CurrentUserProvider.class);
    DemoBillingService service=new DemoBillingService(orders,quotes,accounts,events,scope,users);
    RentalOrder order=new RentalOrder();DemoRentalAccount account=new DemoRentalAccount();
    @BeforeEach void setup() {
        org.springframework.test.util.ReflectionTestUtils.setField(order,"id",1L);
        order.setOrganizationId(1L);order.setBranchId(1L);order.setCustomerId(2L);order.setStatus(OrderStatus.CONFIRMED);
        account.rentalOrderId=1L;account.invoiceIssued=true;account.invoiceAmount=new BigDecimal("1000");
        account.requiredDeposit=new BigDecimal("300");account.dueDate=LocalDate.now().plusDays(1);
        when(orders.findForUpdate(1L)).thenReturn(Optional.of(order));when(orders.findById(1L)).thenReturn(Optional.of(order));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));when(events.findByRentalOrderIdOrderByIdAsc(1L)).thenReturn(List.of());
    }
    DemoBillingService.MoneyInput input(String value,String method) { return new DemoBillingService.MoneyInput("key",new BigDecimal(value),method,"demo"); }
    DemoRentalEvent event(String kind,String amount) { var e=new DemoRentalEvent();e.kind=kind;e.amount=new BigDecimal(amount);return e; }
    @Test void rejectsCodBeforeDelivery() { assertThatThrownBy(()->service.money(1L,"PAYMENT",input("10","COD"))).hasMessageContaining("bàn giao"); }
    @Test void rejectsOverpayment() { assertThatThrownBy(()->service.money(1L,"PAYMENT",input("1001","CASH"))).hasMessageContaining("vượt"); }
    @Test void rejectsExcessDeposit() { assertThatThrownBy(()->service.money(1L,"DEPOSIT_COLLECT",input("301","CASH"))).hasMessageContaining("vượt"); }
    @Test void rejectsRefundBeforeReturn() { assertThatThrownBy(()->service.money(1L,"DEPOSIT_REFUND",input("10","CASH"))).hasMessageContaining("nhận trả"); }
    @Test void rejectsDuplicateInvoice() { assertThatThrownBy(()->service.issue(1L,new DemoBillingService.InvoiceInput(LocalDate.now()))).hasMessageContaining("trùng"); }
    @Test void rejectsNonPositiveMoney() { assertThatThrownBy(()->service.money(1L,"PAYMENT",input("0","CASH"))).hasMessageContaining("dương"); }
    @Test void keepsDepositSeparateFromRentAndComputesDebt() {
        when(events.findByRentalOrderIdOrderByIdAsc(1L)).thenReturn(List.of(event("PAYMENT","200"),event("DEPOSIT_COLLECT","300"),event("DEPOSIT_REFUND","100"),event("DEPOSIT_DEDUCT","50")));
        var value=service.get(1L);assertThat(value.outstandingAmount()).isEqualByComparingTo("800");assertThat(value.heldDeposit()).isEqualByComparingTo("150");
        assertThat(value.invoiceStatus()).isEqualTo("PARTIALLY_PAID");verify(scope).requireRentalAccess(1L,1L,2L);
    }
    @Test void computesOverdueWithoutScheduledJob() { account.dueDate=LocalDate.now().minusDays(1);assertThat(service.get(1L).invoiceStatus()).isEqualTo("OVERDUE"); }
    @Test void duplicateReceiptMustHaveIdenticalPayload() {
        var e=event("PAYMENT","100");e.paymentMethod="CASH";e.note="demo";
        when(events.findByRentalOrderIdAndRequestKey(1L,"key")).thenReturn(Optional.of(e));
        assertThatThrownBy(()->service.money(1L,"PAYMENT",input("101","CASH"))).hasMessageContaining("requestKey");
        service.money(1L,"PAYMENT",input("100","CASH"));verify(events,never()).saveAndFlush(any());
    }
    @Test void cannotCancelInvoiceWithHeldDeposit() {
        when(events.findByRentalOrderIdOrderByIdAsc(1L)).thenReturn(List.of(event("DEPOSIT_COLLECT","300")));
        assertThatThrownBy(()->service.cancelInvoice(1L,"demo")).hasMessageContaining("giữ cọc");
    }
}
