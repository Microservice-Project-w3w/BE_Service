package com.equipmentrental.rental.service;

import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.QuotationCreate;
import com.equipmentrental.rental.dto.request.*;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RentalBusinessRulesTest {
    final RentalRequestRepository requests = mock(RentalRequestRepository.class);
    final QuotationRepository quotations = mock(QuotationRepository.class);
    final RentalOrderRepository orders = mock(RentalOrderRepository.class);
    final RentalContractRepository contracts = mock(RentalContractRepository.class);
    final PricingService pricing = mock(PricingService.class);
    final InventoryClient inventory = mock(InventoryClient.class);
    final RentalDataScopeGuard scope = mock(RentalDataScopeGuard.class);
    final RentalWorkflowService workflow = new RentalWorkflowService(requests, quotations, orders, pricing, scope, inventory, contracts);
    final LocalDateTime start = LocalDateTime.now().plusDays(10);

    RentalRequest request(RequestStatus status) {
        var value = new RentalRequest(); ReflectionTestUtils.setField(value, "id", 1L);
        value.setOrganizationId(1L); value.setBranchId(2L); value.setCustomerId(3L);
        value.setStatus(status); value.setStartAt(start); value.setEndAt(start.plusDays(2));
        var item = new RentalRequestItem(); item.setEquipmentTypeId(31L); item.setQuantity(2); value.addItem(item);
        when(requests.findForUpdate(1L)).thenReturn(Optional.of(value));
        when(requests.findById(1L)).thenReturn(Optional.of(value));
        return value;
    }
    QuotationCreate quotationInput(BigDecimal amount) {
        return new QuotationCreate(1L, amount, BigDecimal.ZERO, BigDecimal.ZERO, null,
                LocalDateTime.now().plusDays(1), "Điều khoản");
    }
    @ParameterizedTest @EnumSource(value = RequestStatus.class, names = {"CANCELLED", "REJECTED"})
    void cannotQuoteClosedRequest(RequestStatus status) {
        request(status);
        assertThrows(ApiException.class, () -> workflow.createQuotation(quotationInput(BigDecimal.TEN)));
        verify(quotations, never()).save(any()); verifyNoInteractions(pricing);
    }
    @Test void cannotCreateSecondActiveQuotationForTheSameRequest() {
        request(RequestStatus.QUOTED);
        when(quotations.existsByRentalRequestIdAndStatusNotIn(eq(1L), anyList())).thenReturn(true);
        assertThrows(ApiException.class, () -> workflow.createQuotation(quotationInput(BigDecimal.TEN)));
        verify(quotations, never()).save(any());
    }
    @Test void rejectedQuotationCanBeReplacedButNotConvertedTwice() {
        request(RequestStatus.QUOTED);
        when(pricing.calculateDiscount(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(quotations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(QuotationStatus.DRAFT, workflow.createQuotation(quotationInput(BigDecimal.TEN)).status());
        var old = quotation(QuotationStatus.CONVERTED);
        assertThrows(ApiException.class, () -> workflow.convertToOrder(10L));
        verify(orders, never()).save(any());
    }
    @Test void cannotCreateNegativeQuotationEvenWhenServiceIsCalledDirectly() {
        request(RequestStatus.SUBMITTED);
        assertThrows(ApiException.class, () -> workflow.createQuotation(quotationInput(BigDecimal.ONE.negate())));
        verify(quotations, never()).save(any());
    }
    Quotation quotation(QuotationStatus status) {
        var value = new Quotation(); value.setOrganizationId(1L); value.setBranchId(2L); value.setCustomerId(3L);
        value.setRentalRequestId(1L); value.setStatus(status); value.setValidUntil(LocalDateTime.now().minusMinutes(1));
        when(quotations.findForUpdate(10L)).thenReturn(Optional.of(value));
        when(quotations.findById(10L)).thenReturn(Optional.of(value)); return value;
    }
    @ParameterizedTest @EnumSource(value = QuotationStatus.class, names = {"DRAFT", "SENT", "ACCEPTED"})
    void expiredQuotationCannotProgress(QuotationStatus status) {
        quotation(status);
        assertThrows(ApiException.class, () -> {
            if (status == QuotationStatus.DRAFT) workflow.sendQuotation(10L);
            else if (status == QuotationStatus.SENT) workflow.approveQuotation(10L);
            else workflow.convertToOrder(10L);
        });
        verify(quotations, never()).save(any()); verify(orders, never()).save(any());
    }
    RentalOrder pendingOrder() throws Exception {
        request(RequestStatus.QUOTED); quotation(QuotationStatus.CONVERTED);
        var value = new RentalOrder(); ReflectionTestUtils.setField(value, "id", 20L);
        value.setOrganizationId(1L); value.setBranchId(2L); value.setCustomerId(3L); value.setQuotationId(10L);
        value.setStatus(OrderStatus.PENDING); value.setStartAt(start); value.setEndAt(start.plusDays(2));
        when(orders.findForUpdate(20L)).thenReturn(Optional.of(value));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventory.availability(eq(1L), eq(2L), eq(31L), any(), any(), eq(2)))
                .thenReturn(new ObjectMapper().readTree("{\"available\":true,\"equipmentIds\":[7,8,9]}"));
        return value;
    }
    @ParameterizedTest @ValueSource(ints = {1, 3})
    void manualSelectionMustHaveExactlyRequestedQuantity(int quantity) throws Exception {
        pendingOrder();
        assertThrows(ApiException.class, () -> workflow.reserve(20L,
                new ReserveOrderRequest(LocalDateTime.now().plusHours(1), List.of(7L, 8L, 9L).subList(0, quantity))));
        verify(inventory, never()).createReservation(any(), any(), anyList());
    }
    @Test void manualSelectionRejectsUnavailableOrWrongTypeEquipment() throws Exception {
        pendingOrder();
        assertThrows(ApiException.class, () -> workflow.reserve(20L,
                new ReserveOrderRequest(LocalDateTime.now().plusHours(1), List.of(7L, 99L))));
        verify(inventory, never()).createReservation(any(), any(), anyList());
    }
    @Test void manualSelectionRejectsDuplicates() throws Exception {
        pendingOrder();
        assertThrows(ApiException.class, () -> workflow.reserve(20L,
                new ReserveOrderRequest(LocalDateTime.now().plusHours(1), List.of(7L, 7L))));
        verify(inventory, never()).createReservation(any(), any(), anyList());
    }
    @Test void manualSelectionAcceptsOtherAvailableIdsOfTheCorrectType() throws Exception {
        var order = pendingOrder(); var deadline = LocalDateTime.now().plusHours(1);
        when(inventory.createReservation(order, deadline, List.of(8L, 9L))).thenReturn("501");
        var result = workflow.reserve(20L, new ReserveOrderRequest(deadline, List.of(8L, 9L)));
        assertEquals(OrderStatus.RESERVED, result.status()); assertEquals("501", order.getInventoryReservationId());
    }
    @Test void fixedDiscountCannotMakeQuotationTotalNegative() {
        var discounts = mock(DiscountCodeRepository.class);
        var discount = new DiscountCode(); discount.setActive(true); discount.setDiscountType(DiscountType.FIXED);
        discount.setDiscountValue(BigDecimal.valueOf(1000)); discount.setValidFrom(start.minusDays(20)); discount.setValidTo(start);
        when(discounts.findByOrganizationIdAndBranchIdAndCodeIgnoreCase(1L, 2L, "DEMO")).thenReturn(Optional.of(discount));
        var service = new PricingService(mock(RentalPriceRepository.class), discounts, scope);
        assertEquals(0, BigDecimal.TEN.compareTo(service.calculateDiscount("DEMO", BigDecimal.TEN, 1L, 2L)));
    }
    @Test void rejectedContractCanBeRevisedAndSubmittedWithTheSameId() {
        var contract = rejectedContract(); var order = new RentalOrder(); order.setStatus(OrderStatus.CONFIRMED);
        when(orders.findForUpdate(20L)).thenReturn(Optional.of(order));
        var service = new ContractService(contracts, mock(ContractAppendixRepository.class), orders, scope, inventory);
        var result = service.resubmit(30L, new ContractRevisionRequest("  Điều khoản đã sửa  "));
        assertEquals(30L, result.id()); assertEquals(ContractStatus.PENDING_APPROVAL, result.status());
        assertEquals("Điều khoản đã sửa", contract.getTerms()); assertNull(contract.getRejectionReason());
    }
    @Test void cannotResubmitContractIfOrderWasCancelled() {
        rejectedContract(); var order = new RentalOrder(); order.setStatus(OrderStatus.CANCELLED);
        when(orders.findForUpdate(20L)).thenReturn(Optional.of(order));
        var service = new ContractService(contracts, mock(ContractAppendixRepository.class), orders, scope, inventory);
        assertThrows(ApiException.class, () -> service.resubmit(30L, new ContractRevisionRequest("Điều khoản")));
        verify(contracts, never()).save(any());
    }
    RentalContract rejectedContract() {
        var contract = new RentalContract(); ReflectionTestUtils.setField(contract, "id", 30L);
        contract.setOrganizationId(1L); contract.setBranchId(2L); contract.setCustomerId(3L);
        contract.setRentalOrderId(20L); contract.reject("Sửa điều khoản");
        when(contracts.findRentalOrderId(30L)).thenReturn(Optional.of(20L));
        when(contracts.findForUpdate(30L)).thenReturn(Optional.of(contract));
        when(contracts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return contract;
    }
}
