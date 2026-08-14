package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.dto.request.CreateInvoiceRequest;
import com.equipmentrental.billing.dto.request.PaymentRequest;
import com.equipmentrental.billing.dto.response.InvoiceResponse;
import com.equipmentrental.billing.entity.Invoice;
import com.equipmentrental.billing.entity.Payment;
import com.equipmentrental.billing.service.BillingService;
import com.equipmentrental.billing.dto.request.UpdateInvoiceRequest;
import com.equipmentrental.billing.dto.request.CreateInvoiceItemRequest;
import com.equipmentrental.billing.dto.request.CreatePaymentRequest;
import com.equipmentrental.billing.dto.response.RentalOrderBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.PaymentResponse;
import com.equipmentrental.billing.dto.request.CancelInvoiceRequest;
import com.equipmentrental.billing.dto.request.RefundPaymentRequest;
import com.equipmentrental.billing.dto.response.PaymentRefundResponse;
import com.equipmentrental.billing.dto.response.InvoicePaymentStatusResponse;
import com.equipmentrental.billing.dto.response.DebtResponse;
import com.equipmentrental.billing.dto.request.UpdateDebtRequest;
import com.equipmentrental.billing.dto.request.SettleDebtRequest;
import com.equipmentrental.billing.dto.response.CustomerBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.RentalContractBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.DebtReportResponse;
import jakarta.validation.Valid;
import com.equipmentrental.billing.dto.response.RevenueReportResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.equipmentrental.billing.dto.response.PaymentReportResponse;
import com.equipmentrental.billing.dto.response.DepositReportResponse;
import com.equipmentrental.billing.dto.response.InvoiceHistoryResponse;
import com.equipmentrental.billing.dto.response.DepositHistoryResponse;
import com.equipmentrental.billing.dto.response.CustomerBillingTransactionResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/invoices/generate/{rentalOrderId}")
    public ResponseEntity<Invoice> generateInvoice(
            @PathVariable Long rentalOrderId
    ) {

        Invoice invoice =
                billingService.generateInvoice(
                        rentalOrderId
                );

        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<Payment> processPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request
    ) {

        Payment payment =
                billingService.processPayment(
                        invoiceId,
                        request
                );

        return ResponseEntity.ok(payment);
    }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request
    ) {

        return billingService.createInvoice(
                request
        );
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> getAllInvoices() {
        return billingService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public InvoiceResponse getInvoiceById(
            @PathVariable Long id
    ) {
        return billingService.getInvoiceById(id);
    }

    @PutMapping("/invoices/{id}")
    public InvoiceResponse updateInvoice(
            @PathVariable Long id,
            @RequestBody UpdateInvoiceRequest request
    ) {
        return billingService.updateInvoice(id, request);
    }

    @PostMapping("/invoices/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse addInvoiceItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateInvoiceItemRequest request
    ) {
        return billingService.addInvoiceItem(id, request);
    }

    @DeleteMapping("/invoices/{invoiceId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoiceItem(
            @PathVariable Long invoiceId,
            @PathVariable Long itemId
    ) {
        billingService.deleteInvoiceItem(invoiceId, itemId);
    }

    @PostMapping("/invoices/{id}/issue")
    public InvoiceResponse issueInvoice(
            @PathVariable Long id
    ) {
        return billingService.issueInvoice(id);
    }

    @PostMapping("/invoices/{id}/cancel")
    public InvoiceResponse cancelInvoice(
            @PathVariable Long id,
            @Valid @RequestBody CancelInvoiceRequest request
    ) {
        return billingService.cancelInvoice(id, request);
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return billingService.createPayment(request);
    }

    @GetMapping("/payments")
    public List<PaymentResponse> getAllPayments() {
        return billingService.getAllPayments();
    }

    @GetMapping("/payments/{id}")
    public PaymentResponse getPaymentById(
            @PathVariable Long id
    ) {
        return billingService.getPaymentById(id);
    }
    @PostMapping("/payments/{id}/confirm")
    public PaymentResponse confirmPayment(
            @PathVariable Long id
    ) {
        return billingService.confirmPayment(id);
    }

    @PostMapping("/payments/{id}/cancel")
    public PaymentResponse cancelPayment(
            @PathVariable Long id
    ) {
        return billingService.cancelPayment(id);
    }

    @PostMapping("/payments/{id}/refund")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentRefundResponse refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundPaymentRequest request
    ) {
        return billingService.refundPayment(id, request);
    }

    @GetMapping("/payments/{id}/refunds")
    public List<PaymentRefundResponse> getPaymentRefunds(
            @PathVariable Long id
    ) {
        return billingService.getPaymentRefunds(id);
    }
    @GetMapping("/invoices/{id}/payment-status")
    public InvoicePaymentStatusResponse getInvoicePaymentStatus(
            @PathVariable Long id
    ) {
        return billingService.getInvoicePaymentStatus(id);
    }
    @GetMapping("/debts")
    public List<DebtResponse> getAllDebts() {
        return billingService.getAllDebts();
    }
    @GetMapping("/debts/{id}")
    public DebtResponse getDebtById(
            @PathVariable Long id
    ) {
        return billingService.getDebtById(id);
    }
    @PutMapping("/debts/{id}")
    public DebtResponse updateDebt(
            @PathVariable Long id,
            @RequestBody UpdateDebtRequest request
    ) {
        return billingService.updateDebt(id, request);
    }
    @PostMapping("/debts/{id}/settle")
    public DebtResponse settleDebt(
            @PathVariable Long id,
            @Valid @RequestBody SettleDebtRequest request
    ) {
        return billingService.settleDebt(id, request);
    }

    @GetMapping("/customers/{customerId}/debts")
    public List<DebtResponse> getDebtsByCustomer(
            @PathVariable Long customerId
    ) {
        return billingService.getDebtsByCustomer(customerId);
    }

    @GetMapping("/customers/{customerId}/summary")
    public CustomerBillingSummaryResponse getCustomerBillingSummary(
            @PathVariable Long customerId
    ) {
        return billingService.getCustomerBillingSummary(customerId);
    }

    @GetMapping("/rental-orders/{rentalOrderId}/summary")
    public RentalOrderBillingSummaryResponse getRentalOrderBillingSummary(
            @PathVariable Long rentalOrderId
    ) {
        return billingService.getRentalOrderBillingSummary(rentalOrderId);
    }

    @GetMapping("/rental-contracts/{contractId}/summary")
    public RentalContractBillingSummaryResponse getRentalContractBillingSummary(
            @PathVariable Long contractId
    ) {
        return billingService.getRentalContractBillingSummary(contractId);
    }
    @GetMapping("/reports/revenue")
    public RevenueReportResponse getRevenueReport(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        return billingService.getRevenueReport(
                organizationId,
                branchId,
                fromDate,
                toDate
        );
    }
    @GetMapping("/reports/payments")
    public PaymentReportResponse getPaymentReport(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        return billingService.getPaymentReport(
                organizationId,
                branchId,
                fromDate,
                toDate
        );
    }
    @GetMapping("/reports/debts")
    public DebtReportResponse getDebtReport(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId
    ) {
        return billingService.getDebtReport(
                organizationId,
                branchId
        );
    }

    @GetMapping("/reports/deposits")
    public DepositReportResponse getDepositReport(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId
    ) {
        return billingService.getDepositReport(
                organizationId,
                branchId
        );
    }

    @GetMapping("/invoices/{id}/history")
    public List<InvoiceHistoryResponse> getInvoiceHistory(
            @PathVariable Long id
    ) {
        return billingService.getInvoiceHistory(id);
    }

    @GetMapping("/deposits/{id}/history")
    public List<DepositHistoryResponse> getDepositHistory(
            @PathVariable Long id
    ) {
        return billingService.getDepositHistory(id);
    }

    @GetMapping("/customers/{customerId}/transactions")
    public List<CustomerBillingTransactionResponse> getCustomerTransactions(
            @PathVariable Long customerId
    ) {
        return billingService.getCustomerTransactions(customerId);
    }
}