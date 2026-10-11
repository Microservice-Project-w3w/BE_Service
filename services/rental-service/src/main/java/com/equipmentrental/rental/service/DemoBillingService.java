package com.equipmentrental.rental.service;

import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import com.equipmentrental.rental.exception.ApiException;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemoBillingService {
    private final RentalOrderRepository orders;
    private final QuotationRepository quotations;
    private final DemoRentalAccountRepository accounts;
    private final DemoRentalEventRepository events;
    private final RentalDataScopeGuard scope;
    private final CurrentUserProvider users;
    public DemoBillingService(RentalOrderRepository orders, QuotationRepository quotations,
            DemoRentalAccountRepository accounts, DemoRentalEventRepository events,
            RentalDataScopeGuard scope, CurrentUserProvider users) {
        this.orders=orders; this.quotations=quotations; this.accounts=accounts;
        this.events=events; this.scope=scope; this.users=users;
    }
    public record InvoiceInput(@NotNull LocalDate dueDate) {}
    public record MoneyInput(@NotBlank @Size(max=80) String requestKey,
            @NotNull @DecimalMin("0.01") @Digits(integer=13, fraction=2) BigDecimal amount,
            @NotBlank String paymentMethod, @NotBlank @Size(max=1000) String note) {}
    public record MethodInput(@NotBlank String paymentMethod) {}
    public record Summary(Long rentalOrderId, String orderCode, Long organizationId, Long branchId,
            Long customerId, String invoiceCode, String invoiceStatus, BigDecimal invoiceAmount,
            LocalDate dueDate, String paymentMethod, BigDecimal paidAmount, BigDecimal outstandingAmount,
            BigDecimal requiredDeposit, BigDecimal collectedDeposit, BigDecimal heldDeposit,
            BigDecimal refundedDeposit, BigDecimal deductedDeposit, List<DemoRentalEvent> history) {}

    @Transactional(readOnly=true)
    public Summary get(Long orderId) { return summarize(order(orderId, false)); }
    @Transactional(readOnly=true)
    public List<Summary> list(Long organizationId, Long branchId) {
        Long customerId = scope.customerIdForOwnList(organizationId, branchId);
        var values = customerId == null ? orders.findByOrganizationIdAndBranchId(organizationId, branchId)
                : orders.findByOrganizationIdAndBranchIdAndCustomerId(organizationId, branchId, customerId);
        return values.stream().map(this::summarize).toList();
    }
    public Summary issue(Long orderId, InvoiceInput input) {
        var order=order(orderId, true);
        requireConfirmed(order);
        var account=account(order);
        if (account.invoiceIssued) throw new ApiException("Đơn đã có hóa đơn; không phát hành trùng");
        account.invoiceIssued=true; account.invoiceAmount=order.getTotalAmount(); account.dueDate=input.dueDate();
        accounts.save(account);
        append(orderId, "invoice", "INVOICE_ISSUED", BigDecimal.ZERO, null, "Phát hành hóa đơn nội bộ", null);
        return summarize(order);
    }
    public Summary method(Long orderId, MethodInput input) {
        var order=order(orderId, true); requireConfirmed(order);
        var account=account(order); account.paymentMethod=method(input.paymentMethod());
        accounts.save(account); return summarize(order);
    }
    public Summary money(Long orderId, String kind, MoneyInput input) {
        if (!Set.of("PAYMENT", "DEPOSIT_COLLECT", "DEPOSIT_REFUND", "DEPOSIT_DEDUCT").contains(kind))
            throw new ApiException("Loại giao dịch không hợp lệ");
        var order=order(orderId, true);
        var account=account(order);
        var previous=events.findByRentalOrderIdAndRequestKey(orderId, input.requestKey());
        if (previous.isPresent()) {
            var p=previous.get();
            if (!p.kind.equals(kind) || p.amount.compareTo(input.amount())!=0
                    || !Objects.equals(p.paymentMethod, input.paymentMethod()) || !Objects.equals(p.note, input.note()))
                throw new ApiException("requestKey đã dùng cho nội dung khác");
            return summarize(order);
        }
        positive(input.amount()); method(input.paymentMethod());
        if (input.note()==null || input.note().isBlank()) throw new ApiException("Cần ghi chú/lý do giao dịch");
        var summary=summarize(order);
        switch(kind) {
            case "PAYMENT" -> {
                requireConfirmed(order);
                if (!account.invoiceIssued || account.invoiceCancelled) throw new ApiException("Hóa đơn chưa phát hành hoặc đã hủy");
                atMost(input.amount(), summary.outstandingAmount(), "Thu vượt số tiền còn phải trả");
                if (input.paymentMethod().equals("COD") && summary.history().stream().noneMatch(e -> e.kind.equals("DELIVERED")))
                    throw new ApiException("COD chỉ ghi nhận đã thu sau khi bàn giao thiết bị");
            }
            case "DEPOSIT_COLLECT" -> {
                requireConfirmed(order);
                atMost(input.amount(), account.requiredDeposit.subtract(summary.collectedDeposit()), "Thu vượt số cọc theo báo giá");
            }
            case "DEPOSIT_REFUND", "DEPOSIT_DEDUCT" -> {
                if (order.getStatus()!=OrderStatus.CANCELLED
                        && summary.history().stream().noneMatch(e -> e.kind.equals("RETURNED")))
                    throw new ApiException("Chỉ hoàn/khấu trừ cọc khi đã nhận trả hoặc đơn đã hủy");
                atMost(input.amount(), summary.heldDeposit(), "Vượt số tiền cọc đang giữ");
            }
        }
        append(orderId, input.requestKey(), kind, input.amount(), input.paymentMethod(), input.note(), null);
        return summarize(order);
    }
    public Summary cancelInvoice(Long orderId, String reason) {
        var order=order(orderId, true); var account=account(order);
        var summary=summarize(order);
        if (!account.invoiceIssued || account.invoiceCancelled) throw new ApiException("Không có hóa đơn để hủy");
        if (summary.paidAmount().signum()>0 || summary.heldDeposit().signum()>0
                || summary.history().stream().anyMatch(e -> e.kind.equals("DELIVERED")))
            throw new ApiException("Không hủy hóa đơn đã thu tiền/giữ cọc hoặc đã bàn giao");
        if (reason==null || reason.isBlank() || reason.length()>1000) throw new ApiException("Cần lý do hủy hợp lệ");
        account.invoiceCancelled=true; accounts.save(account);
        append(orderId, "invoice-cancel", "INVOICE_CANCELLED", BigDecimal.ZERO, null, reason, null);
        return summarize(order);
    }
    public RentalOrder order(Long id, boolean lock) {
        var order=(lock ? orders.findForUpdate(id) : orders.findById(id))
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn thuê"));
        scope.requireRentalAccess(order.getOrganizationId(), order.getBranchId(), order.getCustomerId());
        return order;
    }
    public DemoRentalEvent append(Long orderId, String key, String kind, BigDecimal amount,
            String method, String note, String details) {
        var event=new DemoRentalEvent(); event.rentalOrderId=orderId; event.requestKey=key;
        event.kind=kind; event.amount=amount; event.paymentMethod=method; event.note=note; event.details=details;
        event.actorUserId=users.getCurrentUser().userId();
        return events.saveAndFlush(event);
    }
    private DemoRentalAccount account(RentalOrder order) {
        return accounts.findById(order.getId()).orElseGet(() -> {
            var value=new DemoRentalAccount(); value.rentalOrderId=order.getId();
            value.requiredDeposit=quotations.findById(order.getQuotationId()).orElseThrow().getDepositAmount();
            return value;
        });
    }
    private Summary summarize(RentalOrder order) {
        var account=account(order); var history=events.findByRentalOrderIdOrderByIdAsc(order.getId());
        var paid=sum(history,"PAYMENT"); var collected=sum(history,"DEPOSIT_COLLECT");
        var refunded=sum(history,"DEPOSIT_REFUND"); var deducted=sum(history,"DEPOSIT_DEDUCT");
        var outstanding=account.invoiceCancelled ? BigDecimal.ZERO : account.invoiceAmount.subtract(paid);
        String status=!account.invoiceIssued ? "NOT_ISSUED" : account.invoiceCancelled ? "CANCELLED"
                : outstanding.signum()==0 ? "PAID" : account.dueDate.isBefore(LocalDate.now()) ? "OVERDUE"
                : paid.signum()>0 ? "PARTIALLY_PAID" : "UNPAID";
        return new Summary(order.getId(), order.getOrderCode(), order.getOrganizationId(), order.getBranchId(),
                order.getCustomerId(), account.invoiceIssued ? "INV-DEMO-"+order.getId() : null,
                status, account.invoiceAmount, account.dueDate, account.paymentMethod, paid, outstanding,
                account.requiredDeposit, collected, collected.subtract(refunded).subtract(deducted), refunded, deducted, history);
    }
    static BigDecimal sum(List<DemoRentalEvent> history, String kind) {
        return history.stream().filter(e -> e.kind.equals(kind)).map(e -> e.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    static void positive(BigDecimal amount) {
        if (amount==null || amount.signum()<=0 || amount.scale()>2 || amount.precision()-amount.scale()>13)
            throw new ApiException("Số tiền phải dương, tối đa 2 chữ số thập phân");
    }
    static void atMost(BigDecimal amount, BigDecimal max, String message) {
        if (amount.compareTo(max)>0) throw new ApiException(message);
    }
    static void requireConfirmed(RentalOrder order) {
        if (order.getStatus()!=OrderStatus.CONFIRMED) throw new ApiException("Đơn chưa xác nhận hoặc đã hủy");
    }
    static String method(String method) {
        if (!Set.of("COD", "CASH", "BANK_TRANSFER").contains(method)) throw new ApiException("Phương thức không được hỗ trợ");
        return method;
    }
}
