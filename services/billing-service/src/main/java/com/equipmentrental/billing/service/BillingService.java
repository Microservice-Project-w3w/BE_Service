package com.equipmentrental.billing.service;

import com.equipmentrental.billing.client.CustomerClient;
import com.equipmentrental.billing.client.LogisticsClient;
import com.equipmentrental.billing.client.MaintenanceClient;
import com.equipmentrental.billing.client.RentalClient;
import com.equipmentrental.billing.dto.external.CustomerResponse;
import com.equipmentrental.billing.dto.external.LogisticsReturnRecordResponse;
import com.equipmentrental.billing.dto.external.MaintenanceEvaluationResponse;
import com.equipmentrental.billing.dto.external.RentalOrderResponse;
import com.equipmentrental.billing.dto.request.CancelInvoiceRequest;
import com.equipmentrental.billing.dto.request.CreateInvoiceItemRequest;
import com.equipmentrental.billing.dto.request.CreateInvoiceRequest;
import com.equipmentrental.billing.dto.request.CreatePaymentRequest;
import com.equipmentrental.billing.dto.request.PaymentRequest;
import com.equipmentrental.billing.dto.request.UpdateInvoiceRequest;
import com.equipmentrental.billing.dto.response.InvoiceItemResponse;
import com.equipmentrental.billing.dto.response.InvoiceResponse;
import com.equipmentrental.billing.dto.response.PaymentResponse;
import com.equipmentrental.billing.entity.IncurredFee;
import com.equipmentrental.billing.entity.Invoice;
import com.equipmentrental.billing.entity.InvoiceItem;
import com.equipmentrental.billing.entity.Payment;
import com.equipmentrental.billing.entity.Deposit;
import com.equipmentrental.billing.dto.response.CustomerBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.DebtResponse;
import com.equipmentrental.billing.entity.Debt;
import com.equipmentrental.billing.repository.DebtRepository;
import com.equipmentrental.billing.entity.enums.FeeType;
import com.equipmentrental.billing.entity.enums.InvoiceStatus;
import com.equipmentrental.billing.entity.enums.PaymentStatus;
import com.equipmentrental.billing.repository.IncurredFeeRepository;
import com.equipmentrental.billing.repository.InvoiceItemRepository;
import com.equipmentrental.billing.repository.InvoiceRepository;
import com.equipmentrental.billing.repository.PaymentRepository;
import com.equipmentrental.billing.dto.request.RefundPaymentRequest;
import com.equipmentrental.billing.dto.response.PaymentRefundResponse;
import com.equipmentrental.billing.repository.DepositRepository;
import com.equipmentrental.billing.entity.PaymentRefund;
import com.equipmentrental.billing.repository.PaymentRefundRepository;
import com.equipmentrental.billing.dto.response.InvoicePaymentStatusResponse;
import com.equipmentrental.billing.dto.request.UpdateDebtRequest;
import com.equipmentrental.billing.dto.request.SettleDebtRequest;
import com.equipmentrental.billing.dto.response.RevenueReportResponse;
import com.equipmentrental.billing.dto.request.InternalCreateInvoiceRequest;
import com.equipmentrental.billing.dto.request.InternalInvoiceItemRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.equipmentrental.billing.dto.response.RentalOrderBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.RentalContractBillingSummaryResponse;
import com.equipmentrental.billing.dto.response.PaymentReportResponse;
import com.equipmentrental.billing.dto.response.DebtReportResponse;
import com.equipmentrental.billing.dto.response.DepositReportResponse;
import com.equipmentrental.billing.dto.request.InternalAddChargeRequest;
import com.equipmentrental.billing.dto.response.InternalRentalOrderBillingStatusResponse;
import com.equipmentrental.billing.dto.response.InternalContractSettlementStatusResponse;
import com.equipmentrental.billing.dto.response.InternalCustomerDebtStatusResponse;
import com.equipmentrental.billing.dto.response.InvoiceHistoryResponse;
import com.equipmentrental.billing.repository.InvoiceHistoryRepository;
import com.equipmentrental.billing.dto.response.DepositHistoryResponse;
import com.equipmentrental.billing.repository.DepositHistoryRepository;
import com.equipmentrental.billing.dto.response.CustomerBillingTransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final IncurredFeeRepository incurredFeeRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final RentalClient rentalClient;
    private final LogisticsClient logisticsClient;
    private final MaintenanceClient maintenanceClient;
    private final CustomerClient customerClient;
    private final DebtRepository debtRepository;
    private final DepositRepository depositRepository;
    private final InvoiceHistoryRepository invoiceHistoryRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    // =========================================================
    // GENERATE INVOICE CŨ
    // =========================================================

    @Transactional
    public Invoice generateInvoice(Long rentalOrderId) {

        Optional<Invoice> existingInvoice =
                invoiceRepository.findByRentalOrderId(rentalOrderId);

        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }

        RentalOrderResponse rentalData =
                rentalClient.getRentalOrder(rentalOrderId);

        LogisticsReturnRecordResponse logisticsData = null;

        try {
            logisticsData =
                    logisticsClient.getReturnRecordByRentalOrderId(
                            rentalOrderId
                    );
        } catch (Exception ignored) {
        }

        MaintenanceEvaluationResponse maintenanceData = null;

        try {
            maintenanceData =
                    maintenanceClient.getEvaluationByRentalOrderId(
                            rentalOrderId
                    );
        } catch (Exception ignored) {
        }

        CustomerResponse customerData =
                customerClient.getCustomerById(
                        rentalData.getCustomerId()
                );

        Invoice invoice = Invoice.builder()
                .contractId(rentalData.getContractId())
                .rentalOrderId(rentalOrderId)
                .customerId(rentalData.getCustomerId())
                .organizationId(1L)
                .branchId(1L)
                .invoiceType("RENTAL")
                .status(InvoiceStatus.DRAFT)
                .dueDate(LocalDateTime.now().plusDays(7))
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount =
                rentalData.getRentalPrice() != null
                        ? rentalData.getRentalPrice()
                        : BigDecimal.ZERO;

        invoice = invoiceRepository.save(invoice);

        if (logisticsData != null) {

            if (logisticsData.getLateDays() > 0) {

                BigDecimal lateFeeAmount =
                        BigDecimal.valueOf(100)
                                .multiply(
                                        BigDecimal.valueOf(
                                                logisticsData.getLateDays()
                                        )
                                );

                createFee(
                        invoice,
                        FeeType.LATE_RETURN,
                        lateFeeAmount,
                        "Late return: "
                                + logisticsData.getLateDays()
                                + " days"
                );

                totalAmount =
                        totalAmount.add(lateFeeAmount);
            }

            if (logisticsData.getMissingAccessories() != null
                    && !logisticsData.getMissingAccessories().isEmpty()) {

                BigDecimal missingFeeAmount =
                        BigDecimal.valueOf(50)
                                .multiply(
                                        BigDecimal.valueOf(
                                                logisticsData
                                                        .getMissingAccessories()
                                                        .size()
                                        )
                                );

                createFee(
                        invoice,
                        FeeType.MISSING_ACCESSORY,
                        missingFeeAmount,
                        "Missing accessories: "
                                + String.join(
                                ", ",
                                logisticsData.getMissingAccessories()
                        )
                );

                totalAmount =
                        totalAmount.add(missingFeeAmount);
            }
        }

        if (maintenanceData != null
                && maintenanceData.getTotalDamageCost() != null
                && maintenanceData.getTotalDamageCost()
                .compareTo(BigDecimal.ZERO) > 0) {

            createFee(
                    invoice,
                    FeeType.DAMAGE,
                    maintenanceData.getTotalDamageCost(),
                    maintenanceData.getEvaluationDetails()
            );

            totalAmount =
                    totalAmount.add(
                            maintenanceData.getTotalDamageCost()
                    );
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.UNPAID);

        return invoiceRepository.save(invoice);
    }

    private IncurredFee createFee(
            Invoice invoice,
            FeeType type,
            BigDecimal amount,
            String description
    ) {

        IncurredFee fee = IncurredFee.builder()
                .invoice(invoice)
                .feeType(type)
                .amount(amount)
                .description(description)
                .build();

        return incurredFeeRepository.save(fee);
    }

    // =========================================================
    // PROCESS PAYMENT CŨ
    // =========================================================

    @Transactional
    public Payment processPayment(
            Long invoiceId,
            PaymentRequest request
    ) {

        Optional<Payment> existingPayment =
                paymentRepository.findByPaymentReference(
                        request.getPaymentReference()
                );

        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Invoice invoice =
                invoiceRepository.findById(invoiceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + invoiceId
                                )
                        );

        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentReference(
                        request.getPaymentReference()
                )
                .amount(request.getAmount())
                .paymentMethod(
                        request.getPaymentMethod()
                )
                .status(PaymentStatus.SUCCESS)
                .confirmationTime(
                        LocalDateTime.now()
                )
                .build();

        payment = paymentRepository.save(payment);

        updateInvoiceStatus(invoice);

        return payment;
    }

    private void updateInvoiceStatus(
            Invoice invoice
    ) {

        BigDecimal totalPaid =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice()
                                        .getId()
                                        .equals(invoice.getId())
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(Payment::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (totalPaid.compareTo(
                invoice.getTotalAmount()
        ) >= 0) {

            invoice.setStatus(
                    InvoiceStatus.PAID
            );

        } else if (
                totalPaid.compareTo(
                        BigDecimal.ZERO
                ) > 0
        ) {

            invoice.setStatus(
                    InvoiceStatus.PARTIALLY_PAID
            );

        } else {

            invoice.setStatus(
                    InvoiceStatus.UNPAID
            );
        }

        invoiceRepository.save(invoice);
    }

    // =========================================================
    // CREATE INVOICE
    // POST /api/v1/billing/invoices
    // =========================================================

    @Transactional
    public InvoiceResponse createInvoice(
            CreateInvoiceRequest request
    ) {

        Invoice invoice = Invoice.builder()
                .organizationId(
                        request.getOrganizationId()
                )
                .branchId(
                        request.getBranchId()
                )
                .customerId(
                        request.getCustomerId()
                )
                .rentalOrderId(
                        request.getRentalOrderId()
                )
                .contractId(
                        request.getRentalContractId()
                )
                .invoiceType(
                        request.getInvoiceType()
                )
                .status(
                        InvoiceStatus.DRAFT
                )
                .dueDate(
                        request.getDueAt()
                )
                .totalAmount(
                        BigDecimal.ZERO
                )
                .build();

        invoice = invoiceRepository.save(invoice);

        BigDecimal subtotal = BigDecimal.ZERO;

        List<InvoiceItem> savedItems =
                new ArrayList<>();

        for (CreateInvoiceItemRequest itemRequest
                : request.getItems()) {

            BigDecimal amount =
                    itemRequest
                            .getQuantity()
                            .multiply(
                                    itemRequest.getUnitPrice()
                            );

            InvoiceItem item =
                    InvoiceItem.builder()
                            .invoice(invoice)
                            .itemType(
                                    itemRequest.getItemType()
                            )
                            .description(
                                    itemRequest.getDescription()
                            )
                            .quantity(
                                    itemRequest.getQuantity()
                            )
                            .unitPrice(
                                    itemRequest.getUnitPrice()
                            )
                            .amount(amount)
                            .referenceType(
                                    itemRequest.getReferenceType()
                            )
                            .referenceId(
                                    itemRequest.getReferenceId()
                            )
                            .build();

            item =
                    invoiceItemRepository.save(item);

            savedItems.add(item);

            subtotal =
                    subtotal.add(amount);
        }

        invoice.setTotalAmount(subtotal);
        invoice.setItems(savedItems);

        invoice =
                invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // INVOICE MAPPER
    // =========================================================

    private InvoiceResponse mapInvoiceToResponse(
            Invoice invoice
    ) {

        InvoiceResponse response =
                new InvoiceResponse();

        response.setId(
                invoice.getId()
        );

        response.setOrganizationId(
                invoice.getOrganizationId()
        );

        response.setBranchId(
                invoice.getBranchId()
        );

        response.setCustomerId(
                invoice.getCustomerId()
        );

        response.setRentalOrderId(
                invoice.getRentalOrderId()
        );

        response.setRentalContractId(
                invoice.getContractId()
        );

        response.setInvoiceType(
                invoice.getInvoiceType()
        );

        response.setStatus(
                invoice.getStatus().name()
        );

        response.setSubtotal(
                invoice.getTotalAmount()
        );

        response.setTotalAmount(
                invoice.getTotalAmount()
        );

        response.setDueAt(
                invoice.getDueDate()
        );

        List<InvoiceItemResponse> itemResponses =
                invoice.getItems()
                        .stream()
                        .map(item -> {

                            InvoiceItemResponse itemResponse =
                                    new InvoiceItemResponse();

                            itemResponse.setId(
                                    item.getId()
                            );

                            itemResponse.setItemType(
                                    item.getItemType()
                            );

                            itemResponse.setDescription(
                                    item.getDescription()
                            );

                            itemResponse.setQuantity(
                                    item.getQuantity()
                            );

                            itemResponse.setUnitPrice(
                                    item.getUnitPrice()
                            );

                            itemResponse.setAmount(
                                    item.getAmount()
                            );

                            itemResponse.setReferenceType(
                                    item.getReferenceType()
                            );

                            itemResponse.setReferenceId(
                                    item.getReferenceId()
                            );

                            return itemResponse;
                        })
                        .toList();

        response.setItems(itemResponses);

        return response;
    }

    // =========================================================
    // GET ALL INVOICES
    // =========================================================

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {

        return invoiceRepository.findAll()
                .stream()
                .map(this::mapInvoiceToResponse)
                .toList();
    }

    // =========================================================
    // GET INVOICE BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found: " + id
                        )
                );

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // UPDATE INVOICE
    // =========================================================

    @Transactional
    public InvoiceResponse updateInvoice(
            Long id,
            UpdateInvoiceRequest request
    ) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found: " + id
                        )
                );

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT invoice can be updated"
            );
        }

        if (request.getInvoiceType() != null) {
            invoice.setInvoiceType(
                    request.getInvoiceType()
            );
        }

        if (request.getDueAt() != null) {
            invoice.setDueDate(
                    request.getDueAt()
            );
        }

        invoice =
                invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // ADD INVOICE ITEM
    // =========================================================

    @Transactional
    public InvoiceResponse addInvoiceItem(
            Long invoiceId,
            CreateInvoiceItemRequest request
    ) {

        Invoice invoice =
                invoiceRepository.findById(invoiceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + invoiceId
                                )
                        );

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT invoice can add items"
            );
        }

        BigDecimal amount =
                request.getQuantity()
                        .multiply(
                                request.getUnitPrice()
                        );

        InvoiceItem item =
                InvoiceItem.builder()
                        .invoice(invoice)
                        .itemType(
                                request.getItemType()
                        )
                        .description(
                                request.getDescription()
                        )
                        .quantity(
                                request.getQuantity()
                        )
                        .unitPrice(
                                request.getUnitPrice()
                        )
                        .amount(amount)
                        .referenceType(
                                request.getReferenceType()
                        )
                        .referenceId(
                                request.getReferenceId()
                        )
                        .build();

        item =
                invoiceItemRepository.save(item);

        if (invoice.getItems() == null) {
            invoice.setItems(
                    new ArrayList<>()
            );
        }

        invoice.getItems().add(item);

        BigDecimal currentTotal =
                invoice.getTotalAmount() != null
                        ? invoice.getTotalAmount()
                        : BigDecimal.ZERO;

        invoice.setTotalAmount(
                currentTotal.add(amount)
        );

        invoice =
                invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // DELETE INVOICE ITEM
    // =========================================================

    @Transactional
    public void deleteInvoiceItem(
            Long invoiceId,
            Long itemId
    ) {

        Invoice invoice =
                invoiceRepository.findById(invoiceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + invoiceId
                                )
                        );

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT invoice can delete items"
            );
        }

        InvoiceItem item =
                invoiceItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice item not found: "
                                                + itemId
                                )
                        );

        if (!item.getInvoice()
                .getId()
                .equals(invoiceId)) {

            throw new IllegalArgumentException(
                    "Invoice item does not belong to invoice"
            );
        }

        BigDecimal itemAmount =
                item.getAmount() != null
                        ? item.getAmount()
                        : BigDecimal.ZERO;

        BigDecimal newTotal =
                invoice.getTotalAmount()
                        .subtract(itemAmount);

        if (newTotal.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            newTotal =
                    BigDecimal.ZERO;
        }

        invoice.setTotalAmount(
                newTotal
        );

        if (invoice.getItems() != null) {

            invoice.getItems()
                    .removeIf(existingItem ->
                            existingItem
                                    .getId()
                                    .equals(itemId)
                    );
        }

        invoiceItemRepository.delete(item);

        invoiceRepository.save(invoice);
    }

    // =========================================================
    // ISSUE INVOICE
    // =========================================================

    @Transactional
    public InvoiceResponse issueInvoice(Long id) {

        Invoice invoice =
                invoiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + id
                                )
                        );

        if (invoice.getStatus()
                == InvoiceStatus.ISSUED) {

            return mapInvoiceToResponse(
                    invoice
            );
        }

        if (invoice.getStatus()
                != InvoiceStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only DRAFT invoice can be issued"
            );
        }

        if (invoice.getItems() == null
                || invoice.getItems().isEmpty()) {

            throw new IllegalStateException(
                    "Invoice must contain at least one item before issue"
            );
        }

        invoice.setStatus(
                InvoiceStatus.ISSUED
        );

        invoice =
                invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // CANCEL INVOICE
    // =========================================================

    @Transactional
    public InvoiceResponse cancelInvoice(
            Long id,
            CancelInvoiceRequest request
    ) {

        Invoice invoice =
                invoiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + id
                                )
                        );

        if (invoice.getStatus()
                == InvoiceStatus.CANCELLED) {

            return mapInvoiceToResponse(
                    invoice
            );
        }

        if (invoice.getStatus()
                == InvoiceStatus.PAID) {

            throw new IllegalStateException(
                    "Paid invoice cannot be cancelled"
            );
        }

        invoice.setStatus(
                InvoiceStatus.CANCELLED
        );

        invoice =
                invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    // =========================================================
    // CREATE PAYMENT
    // POST /api/v1/billing/payments
    // =========================================================

    @Transactional
    public PaymentResponse createPayment(
            CreatePaymentRequest request
    ) {

        Invoice invoice =
                invoiceRepository.findById(
                                request.getInvoiceId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice not found: "
                                                + request.getInvoiceId()
                                )
                        );

        if (invoice.getStatus()
                == InvoiceStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Cancelled invoice cannot be paid"
            );
        }

        Payment payment =
                Payment.builder()
                        .invoice(invoice)
                        .paymentReference(
                                request.getTransactionReference()
                        )
                        .amount(
                                request.getAmount()
                        )
                        .paymentMethod(
                                request.getPaymentMethod()
                        )
                        .status(
                                PaymentStatus.PENDING
                        )
                        .confirmationTime(null)
                        .build();

        payment =
                paymentRepository.save(payment);

        PaymentResponse response =
                new PaymentResponse();

        response.setId(
                payment.getId()
        );

        response.setOrganizationId(
                request.getOrganizationId()
        );

        response.setBranchId(
                request.getBranchId()
        );

        response.setCustomerId(
                request.getCustomerId()
        );

        response.setInvoiceId(
                invoice.getId()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod().name()
        );

        response.setTransactionReference(
                payment.getPaymentReference()
        );

        response.setStatus(
                payment.getStatus().name()
        );

        response.setPaidAt(
                request.getPaidAt()
        );

        return response;
    }

    // =========================================================
    // GET ALL PAYMENTS
    // GET /api/v1/billing/payments
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::mapPaymentToResponse)
                .toList();
    }

    // =========================================================
    // PAYMENT MAPPER
    // =========================================================

    private PaymentResponse mapPaymentToResponse(
            Payment payment
    ) {

        PaymentResponse response =
                new PaymentResponse();

        response.setId(
                payment.getId()
        );

        if (payment.getInvoice() != null) {

            response.setInvoiceId(
                    payment.getInvoice().getId()
            );

            response.setOrganizationId(
                    payment.getInvoice()
                            .getOrganizationId()
            );

            response.setBranchId(
                    payment.getInvoice()
                            .getBranchId()
            );

            response.setCustomerId(
                    payment.getInvoice()
                            .getCustomerId()
            );
        }

        response.setAmount(
                payment.getAmount()
        );

        if (payment.getPaymentMethod() != null) {

            response.setPaymentMethod(
                    payment.getPaymentMethod().name()
            );
        }

        response.setTransactionReference(
                payment.getPaymentReference()
        );

        if (payment.getStatus() != null) {

            response.setStatus(
                    payment.getStatus().name()
            );
        }

        response.setPaidAt(
                payment.getConfirmationTime()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found: " + id
                        )
                );

        return mapPaymentToResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found: " + id
                        )
                );

        // Nếu đã xác nhận rồi thì trả lại luôn
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return mapPaymentToResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payment can be confirmed"
            );
        }

        Invoice invoice = payment.getInvoice();

        if (invoice == null) {
            throw new IllegalStateException(
                    "Payment does not belong to an invoice"
            );
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled invoice cannot receive payment"
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setConfirmationTime(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        updateInvoiceStatus(invoice);

        return mapPaymentToResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found: " + id
                        )
                );

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return mapPaymentToResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payment can be cancelled"
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);

        payment = paymentRepository.save(payment);

        return mapPaymentToResponse(payment);
    }
    @Transactional
    public PaymentRefundResponse refundPayment(
            Long paymentId,
            RefundPaymentRequest request
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found: " + paymentId
                        )
                );

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Only SUCCESS payment can be refunded"
            );
        }

        BigDecimal alreadyRefunded =
                paymentRefundRepository.findByPaymentId(paymentId)
                        .stream()
                        .map(PaymentRefund::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundableAmount =
                payment.getAmount()
                        .subtract(alreadyRefunded);

        if (request.getAmount()
                .compareTo(refundableAmount) > 0) {

            throw new IllegalArgumentException(
                    "Refund amount exceeds refundable amount"
            );
        }

        PaymentRefund refund =
                PaymentRefund.builder()
                        .payment(payment)
                        .amount(request.getAmount())
                        .reason(request.getReason())
                        .actorUserId(
                                request.getActorUserId()
                        )
                        .refundedAt(
                                LocalDateTime.now()
                        )
                        .build();

        refund =
                paymentRefundRepository.save(refund);

        return mapPaymentRefundToResponse(refund);
    }
    private PaymentRefundResponse mapPaymentRefundToResponse(
            PaymentRefund refund
    ) {

        PaymentRefundResponse response =
                new PaymentRefundResponse();

        response.setId(refund.getId());
        response.setPaymentId(
                refund.getPayment().getId()
        );

        response.setAmount(
                refund.getAmount()
        );

        response.setReason(
                refund.getReason()
        );

        response.setActorUserId(
                refund.getActorUserId()
        );

        response.setRefundedAt(
                refund.getRefundedAt()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<PaymentRefundResponse> getPaymentRefunds(
            Long paymentId
    ) {

        if (!paymentRepository.existsById(paymentId)) {
            throw new RuntimeException(
                    "Payment not found: " + paymentId
            );
        }

        return paymentRefundRepository
                .findByPaymentId(paymentId)
                .stream()
                .map(this::mapPaymentRefundToResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public InvoicePaymentStatusResponse getInvoicePaymentStatus(
            Long invoiceId
    ) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found: " + invoiceId
                        )
                );

        BigDecimal totalAmount =
                invoice.getTotalAmount() != null
                        ? invoice.getTotalAmount()
                        : BigDecimal.ZERO;

        BigDecimal paidAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getId()
                                        .equals(invoiceId)
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(Payment::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundedAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getId()
                                        .equals(invoiceId)
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .map(PaymentRefund::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal netPaidAmount =
                paidAmount.subtract(refundedAmount);

        if (netPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            netPaidAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount =
                totalAmount.subtract(netPaidAmount);

        String paymentStatus;

        if (netPaidAmount.compareTo(totalAmount) > 0) {

            paymentStatus = "OVERPAID";

            remainingAmount = BigDecimal.ZERO;

        } else if (netPaidAmount.compareTo(totalAmount) == 0
                && totalAmount.compareTo(BigDecimal.ZERO) > 0) {

            paymentStatus = "PAID";

            remainingAmount = BigDecimal.ZERO;

        } else if (netPaidAmount.compareTo(BigDecimal.ZERO) > 0) {

            paymentStatus = "PARTIALLY_PAID";

        } else {

            paymentStatus = "UNPAID";
        }

        InvoicePaymentStatusResponse response =
                new InvoicePaymentStatusResponse();

        response.setInvoiceId(invoice.getId());

        response.setTotalAmount(totalAmount);

        response.setPaidAmount(netPaidAmount);

        response.setRemainingAmount(remainingAmount);

        response.setPaymentStatus(paymentStatus);

        return response;
    }
    @Transactional(readOnly = true)
    public List<DebtResponse> getAllDebts() {

        return debtRepository.findAll()
                .stream()
                .map(this::mapDebtToResponse)
                .toList();
    }

    private DebtResponse mapDebtToResponse(
            Debt debt
    ) {

        DebtResponse response =
                new DebtResponse();

        response.setId(debt.getId());

        response.setOrganizationId(
                debt.getOrganizationId()
        );

        response.setBranchId(
                debt.getBranchId()
        );

        response.setCustomerId(
                debt.getCustomerId()
        );

        response.setInvoiceId(
                debt.getInvoiceId()
        );

        response.setAmount(
                debt.getAmount()
        );

        response.setRemainingAmount(
                debt.getRemainingAmount()
        );

        response.setDueAt(
                debt.getDueAt()
        );

        response.setReason(
                debt.getReason()
        );

        response.setStatus(
                debt.getStatus()
        );

        response.setCreatedAt(
                debt.getCreatedAt()
        );

        response.setUpdatedAt(
                debt.getUpdatedAt()
        );

        return response;
    }
    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id) {

        Debt debt = debtRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Debt not found: " + id
                        )
                );

        return mapDebtToResponse(debt);
    }

    @Transactional
    public DebtResponse updateDebt(
            Long id,
            UpdateDebtRequest request
    ) {

        Debt debt = debtRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Debt not found: " + id
                        )
                );

        if (request.getAmount() != null) {

            BigDecimal paidAmount =
                    debt.getAmount()
                            .subtract(debt.getRemainingAmount());

            debt.setAmount(
                    request.getAmount()
            );

            BigDecimal newRemaining =
                    request.getAmount()
                            .subtract(paidAmount);

            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                newRemaining = BigDecimal.ZERO;
            }

            debt.setRemainingAmount(
                    newRemaining
            );
        }

        if (request.getDueAt() != null) {
            debt.setDueAt(
                    request.getDueAt()
            );
        }

        if (request.getReason() != null
                && !request.getReason().isBlank()) {

            debt.setReason(
                    request.getReason()
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            debt.setStatus(
                    request.getStatus()
            );
        }

        debt = debtRepository.save(debt);

        return mapDebtToResponse(debt);
    }
    @Transactional
    public DebtResponse settleDebt(
            Long id,
            SettleDebtRequest request
    ) {

        Debt debt = debtRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Debt not found: " + id
                        )
                );

        if ("SETTLED".equalsIgnoreCase(debt.getStatus())) {
            return mapDebtToResponse(debt);
        }

        if (request.getAmount()
                .compareTo(debt.getRemainingAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Settlement amount exceeds remaining debt"
            );
        }

        BigDecimal newRemaining =
                debt.getRemainingAmount()
                        .subtract(request.getAmount());

        debt.setRemainingAmount(newRemaining);

        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus("SETTLED");
        } else {
            debt.setStatus("PARTIALLY_SETTLED");
        }

        debt = debtRepository.save(debt);

        return mapDebtToResponse(debt);
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getDebtsByCustomer(
            Long customerId
    ) {

        return debtRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapDebtToResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public CustomerBillingSummaryResponse getCustomerBillingSummary(
            Long customerId
    ) {

        BigDecimal totalInvoiced =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getCustomerId() != null
                                        && invoice.getCustomerId()
                                        .equals(customerId)
                                        && invoice.getStatus()
                                        != InvoiceStatus.CANCELLED
                        )
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalPaid =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getCustomerId() != null
                                        && payment.getInvoice()
                                        .getCustomerId()
                                        .equals(customerId)
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalRefunded =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getCustomerId() != null
                                        && payment.getInvoice()
                                        .getCustomerId()
                                        .equals(customerId)
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal netPaid =
                totalPaid.subtract(totalRefunded);

        if (netPaid.compareTo(BigDecimal.ZERO) < 0) {
            netPaid = BigDecimal.ZERO;
        }

        BigDecimal outstandingDebt =
                debtRepository.findByCustomerId(customerId)
                        .stream()
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal depositHeld =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getCustomerId() != null
                                        && deposit.getCustomerId()
                                        .equals(customerId)
                        )
                        .filter(deposit ->
                                deposit.getStatus() != null
                                        && (
                                        "HELD".equalsIgnoreCase(
                                                deposit.getStatus()
                                        )
                                                || "PARTIALLY_DEDUCTED"
                                                .equalsIgnoreCase(
                                                        deposit.getStatus()
                                                )
                                )
                        )
                        .map(deposit -> {

                            BigDecimal amount =
                                    deposit.getAmount() != null
                                            ? deposit.getAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal deducted =
                                    deposit.getDeductedAmount() != null
                                            ? deposit.getDeductedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal refunded =
                                    deposit.getRefundedAmount() != null
                                            ? deposit.getRefundedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal remaining =
                                    amount
                                            .subtract(deducted)
                                            .subtract(refunded);

                            return remaining.compareTo(
                                    BigDecimal.ZERO
                            ) > 0
                                    ? remaining
                                    : BigDecimal.ZERO;
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        CustomerBillingSummaryResponse response =
                new CustomerBillingSummaryResponse();

        response.setCustomerId(customerId);
        response.setTotalInvoiced(totalInvoiced);
        response.setTotalPaid(netPaid);
        response.setOutstandingDebt(outstandingDebt);
        response.setDepositHeld(depositHeld);

        return response;
    }

    @Transactional(readOnly = true)
    public RentalOrderBillingSummaryResponse getRentalOrderBillingSummary(
            Long rentalOrderId
    ) {

        // =====================================================
        // 1. Tổng tiền hóa đơn theo Rental Order
        // =====================================================

        List<Invoice> invoices =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getRentalOrderId() != null
                                        && invoice.getRentalOrderId()
                                        .equals(rentalOrderId)
                                        && invoice.getStatus()
                                        != InvoiceStatus.CANCELLED
                        )
                        .toList();

        BigDecimal totalInvoiceAmount =
                invoices.stream()
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // 2. Tổng tiền đã thanh toán SUCCESS
        // =====================================================

        BigDecimal grossPaidAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getRentalOrderId() != null
                                        && payment.getInvoice()
                                        .getRentalOrderId()
                                        .equals(rentalOrderId)
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // 3. Trừ số tiền refund
        // =====================================================

        BigDecimal refundedPaymentAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && payment.getInvoice()
                                        .getRentalOrderId() != null
                                        && payment.getInvoice()
                                        .getRentalOrderId()
                                        .equals(rentalOrderId)
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal paidAmount =
                grossPaidAmount.subtract(refundedPaymentAmount);

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            paidAmount = BigDecimal.ZERO;
        }

        // =====================================================
        // 4. Số tiền còn phải thanh toán
        // =====================================================

        BigDecimal remainingAmount =
                totalInvoiceAmount.subtract(paidAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        // =====================================================
        // 5. Deposit của Rental Order
        // =====================================================

        List<Deposit> deposits =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getRentalOrderId() != null
                                        && deposit.getRentalOrderId()
                                        .equals(rentalOrderId)
                        )
                        .toList();

        BigDecimal depositAmount =
                deposits.stream()
                        .map(deposit ->
                                deposit.getAmount() != null
                                        ? deposit.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal deductedDeposit =
                deposits.stream()
                        .map(deposit ->
                                deposit.getDeductedAmount() != null
                                        ? deposit.getDeductedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundedDeposit =
                deposits.stream()
                        .map(deposit ->
                                deposit.getRefundedAmount() != null
                                        ? deposit.getRefundedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // 6. Công nợ liên quan các invoice của Rental Order
        // =====================================================

        List<Long> invoiceIds =
                invoices.stream()
                        .map(Invoice::getId)
                        .toList();

        BigDecimal outstandingDebt =
                debtRepository.findAll()
                        .stream()
                        .filter(debt ->
                                debt.getInvoiceId() != null
                                        && invoiceIds.contains(
                                        debt.getInvoiceId()
                                )
                        )
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // 7. Response
        // =====================================================

        RentalOrderBillingSummaryResponse response =
                new RentalOrderBillingSummaryResponse();

        response.setRentalOrderId(rentalOrderId);
        response.setTotalInvoiceAmount(totalInvoiceAmount);
        response.setPaidAmount(paidAmount);
        response.setRemainingAmount(remainingAmount);
        response.setDepositAmount(depositAmount);
        response.setDeductedDeposit(deductedDeposit);
        response.setRefundedDeposit(refundedDeposit);
        response.setOutstandingDebt(outstandingDebt);

        return response;
    }

    @Transactional(readOnly = true)
    public RentalContractBillingSummaryResponse getRentalContractBillingSummary(
            Long contractId
    ) {

        // 1. Lấy toàn bộ invoice của hợp đồng, bỏ invoice CANCELLED
        List<Invoice> invoices =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getContractId() != null
                                        && invoice.getContractId()
                                        .equals(contractId)
                                        && invoice.getStatus()
                                        != InvoiceStatus.CANCELLED
                        )
                        .toList();

        // 2. Tổng tiền hóa đơn
        BigDecimal totalInvoiceAmount =
                invoices.stream()
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // 3. Lấy danh sách invoiceId của contract
        List<Long> invoiceIds =
                invoices.stream()
                        .map(Invoice::getId)
                        .toList();

        // 4. Tổng payment SUCCESS
        BigDecimal grossPaidAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // 5. Tổng refund payment
        BigDecimal refundedPaymentAmount =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal paidAmount =
                grossPaidAmount.subtract(
                        refundedPaymentAmount
                );

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            paidAmount = BigDecimal.ZERO;
        }

        // 6. Số tiền còn thiếu
        BigDecimal remainingAmount =
                totalInvoiceAmount.subtract(
                        paidAmount
                );

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        // 7. Deposit theo contract
        List<Deposit> deposits =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getRentalContractId() != null
                                        && deposit.getRentalContractId()
                                        .equals(contractId)
                        )
                        .toList();

        BigDecimal depositAmount =
                deposits.stream()
                        .map(deposit ->
                                deposit.getAmount() != null
                                        ? deposit.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal deductedDeposit =
                deposits.stream()
                        .map(deposit ->
                                deposit.getDeductedAmount() != null
                                        ? deposit.getDeductedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundedDeposit =
                deposits.stream()
                        .map(deposit ->
                                deposit.getRefundedAmount() != null
                                        ? deposit.getRefundedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // 8. Công nợ liên quan các invoice của hợp đồng
        BigDecimal outstandingDebt =
                debtRepository.findAll()
                        .stream()
                        .filter(debt ->
                                debt.getInvoiceId() != null
                                        && invoiceIds.contains(
                                        debt.getInvoiceId()
                                )
                        )
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // 9. Response
        RentalContractBillingSummaryResponse response =
                new RentalContractBillingSummaryResponse();

        response.setContractId(contractId);
        response.setTotalInvoiceAmount(totalInvoiceAmount);
        response.setPaidAmount(paidAmount);
        response.setRemainingAmount(remainingAmount);
        response.setDepositAmount(depositAmount);
        response.setDeductedDeposit(deductedDeposit);
        response.setRefundedDeposit(refundedDeposit);
        response.setOutstandingDebt(outstandingDebt);

        return response;
    }
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(
            Long organizationId,
            Long branchId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate must not be after toDate"
            );
        }

        LocalDateTime start =
                fromDate.atStartOfDay();

        LocalDateTime end =
                toDate.plusDays(1).atStartOfDay();

        // =====================================================
        // 1. Invoice phù hợp organization/branch/date
        // =====================================================

        List<Invoice> invoices =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getOrganizationId() != null
                                        && invoice.getOrganizationId()
                                        .equals(organizationId)
                        )
                        .filter(invoice ->
                                branchId == null
                                        || (
                                        invoice.getBranchId() != null
                                                && invoice.getBranchId()
                                                .equals(branchId)
                                )
                        )
                        .filter(invoice ->
                                invoice.getCreatedAt() != null
                                        && !invoice.getCreatedAt()
                                        .isBefore(start)
                                        && invoice.getCreatedAt()
                                        .isBefore(end)
                        )
                        .filter(invoice ->
                                invoice.getStatus()
                                        != InvoiceStatus.CANCELLED
                        )
                        .toList();

        BigDecimal totalInvoiced =
                invoices.stream()
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<Long> invoiceIds =
                invoices.stream()
                        .map(Invoice::getId)
                        .toList();

        // =====================================================
        // 2. Payment SUCCESS
        // =====================================================

        List<Payment> successfulPayments =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                        )
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .filter(payment -> {

                            LocalDateTime paymentTime =
                                    payment.getConfirmationTime();

                            return paymentTime != null
                                    && !paymentTime.isBefore(start)
                                    && paymentTime.isBefore(end);
                        })
                        .toList();

        BigDecimal totalPaid =
                successfulPayments.stream()
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // 3. Refund
        // =====================================================

        BigDecimal totalRefunded =
                successfulPayments.stream()
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .filter(refund ->
                                refund.getRefundedAt() != null
                                        && !refund.getRefundedAt()
                                        .isBefore(start)
                                        && refund.getRefundedAt()
                                        .isBefore(end)
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal netRevenue =
                totalPaid.subtract(totalRefunded);

        if (netRevenue.compareTo(BigDecimal.ZERO) < 0) {
            netRevenue = BigDecimal.ZERO;
        }

        // =====================================================
        // 4. Response
        // =====================================================

        RevenueReportResponse response =
                new RevenueReportResponse();

        response.setOrganizationId(organizationId);
        response.setBranchId(branchId);
        response.setFromDate(fromDate);
        response.setToDate(toDate);

        response.setTotalInvoiced(totalInvoiced);
        response.setTotalPaid(totalPaid);
        response.setTotalRefunded(totalRefunded);
        response.setNetRevenue(netRevenue);

        response.setInvoiceCount(
                (long) invoices.size()
        );

        response.setPaymentCount(
                (long) successfulPayments.size()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public PaymentReportResponse getPaymentReport(
            Long organizationId,
            Long branchId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate must not be after toDate"
            );
        }

        LocalDateTime start =
                fromDate.atStartOfDay();

        LocalDateTime end =
                toDate.plusDays(1).atStartOfDay();

        List<Payment> payments =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                        )
                        .filter(payment ->
                                payment.getInvoice()
                                        .getOrganizationId() != null
                                        && payment.getInvoice()
                                        .getOrganizationId()
                                        .equals(organizationId)
                        )
                        .filter(payment ->
                                branchId == null
                                        || (
                                        payment.getInvoice()
                                                .getBranchId() != null
                                                && payment.getInvoice()
                                                .getBranchId()
                                                .equals(branchId)
                                )
                        )
                        .filter(payment -> {

                            LocalDateTime time =
                                    payment.getConfirmationTime() != null
                                            ? payment.getConfirmationTime()
                                            : payment.getCreatedAt();

                            return time != null
                                    && !time.isBefore(start)
                                    && time.isBefore(end);
                        })
                        .toList();

        BigDecimal totalAmount =
                payments.stream()
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal confirmedAmount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal pendingAmount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.PENDING
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal cancelledAmount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.CANCELLED
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundedAmount =
                payments.stream()
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(
                                                payment.getId()
                                        )
                                        .stream()
                        )
                        .filter(refund ->
                                refund.getRefundedAt() != null
                                        && !refund.getRefundedAt()
                                        .isBefore(start)
                                        && refund.getRefundedAt()
                                        .isBefore(end)
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        long confirmedCount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .count();

        long pendingCount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.PENDING
                        )
                        .count();

        long cancelledCount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.CANCELLED
                        )
                        .count();

        PaymentReportResponse response =
                new PaymentReportResponse();

        response.setOrganizationId(organizationId);
        response.setBranchId(branchId);

        response.setFromDate(fromDate);
        response.setToDate(toDate);

        response.setTotalAmount(totalAmount);
        response.setConfirmedAmount(confirmedAmount);
        response.setPendingAmount(pendingAmount);
        response.setCancelledAmount(cancelledAmount);
        response.setRefundedAmount(refundedAmount);

        response.setTotalPaymentCount(
                (long) payments.size()
        );

        response.setConfirmedPaymentCount(
                confirmedCount
        );

        response.setPendingPaymentCount(
                pendingCount
        );

        response.setCancelledPaymentCount(
                cancelledCount
        );

        return response;
    }

    @Transactional(readOnly = true)
    public DebtReportResponse getDebtReport(
            Long organizationId,
            Long branchId
    ) {

        LocalDateTime now = LocalDateTime.now();

        List<Debt> debts =
                debtRepository.findAll()
                        .stream()
                        .filter(debt ->
                                debt.getOrganizationId() != null
                                        && debt.getOrganizationId()
                                        .equals(organizationId)
                        )
                        .filter(debt ->
                                branchId == null
                                        || (
                                        debt.getBranchId() != null
                                                && debt.getBranchId()
                                                .equals(branchId)
                                )
                        )
                        .toList();

        BigDecimal totalDebt =
                debts.stream()
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<Debt> overdueDebts =
                debts.stream()
                        .filter(debt ->
                                debt.getDueAt() != null
                                        && debt.getDueAt().isBefore(now)
                        )
                        .filter(debt ->
                                debt.getRemainingAmount() != null
                                        && debt.getRemainingAmount()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .toList();

        BigDecimal overdueDebt =
                overdueDebts.stream()
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        long customerCount =
                debts.stream()
                        .filter(debt ->
                                debt.getRemainingAmount() != null
                                        && debt.getRemainingAmount()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .map(Debt::getCustomerId)
                        .filter(customerId ->
                                customerId != null
                        )
                        .distinct()
                        .count();

        long debtCount =
                debts.stream()
                        .filter(debt ->
                                debt.getRemainingAmount() != null
                                        && debt.getRemainingAmount()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .count();

        DebtReportResponse response =
                new DebtReportResponse();

        response.setOrganizationId(organizationId);
        response.setBranchId(branchId);

        response.setTotalDebt(totalDebt);
        response.setOverdueDebt(overdueDebt);

        response.setCustomerCount(customerCount);
        response.setDebtCount(debtCount);

        response.setOverdueDebtCount(
                (long) overdueDebts.size()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public DepositReportResponse getDepositReport(
            Long organizationId,
            Long branchId
    ) {

        List<Deposit> deposits =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getOrganizationId() != null
                                        && deposit.getOrganizationId()
                                        .equals(organizationId)
                        )
                        .filter(deposit ->
                                branchId == null
                                        || (
                                        deposit.getBranchId() != null
                                                && deposit.getBranchId()
                                                .equals(branchId)
                                )
                        )
                        .toList();

        BigDecimal totalCollected =
                deposits.stream()
                        .map(deposit ->
                                deposit.getAmount() != null
                                        ? deposit.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalDeducted =
                deposits.stream()
                        .map(deposit ->
                                deposit.getDeductedAmount() != null
                                        ? deposit.getDeductedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalRefunded =
                deposits.stream()
                        .map(deposit ->
                                deposit.getRefundedAmount() != null
                                        ? deposit.getRefundedAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalHeld =
                deposits.stream()
                        .map(deposit -> {

                            BigDecimal amount =
                                    deposit.getAmount() != null
                                            ? deposit.getAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal deducted =
                                    deposit.getDeductedAmount() != null
                                            ? deposit.getDeductedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal refunded =
                                    deposit.getRefundedAmount() != null
                                            ? deposit.getRefundedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal remaining =
                                    amount
                                            .subtract(deducted)
                                            .subtract(refunded);

                            return remaining.compareTo(BigDecimal.ZERO) > 0
                                    ? remaining
                                    : BigDecimal.ZERO;
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        DepositReportResponse response =
                new DepositReportResponse();

        response.setOrganizationId(organizationId);
        response.setBranchId(branchId);

        response.setTotalCollected(totalCollected);
        response.setTotalHeld(totalHeld);
        response.setTotalDeducted(totalDeducted);
        response.setTotalRefunded(totalRefunded);

        response.setDepositCount(
                (long) deposits.size()
        );

        return response;
    }

    @Transactional
    public InvoiceResponse createInternalInvoice(
            InternalCreateInvoiceRequest request
    ) {

        Optional<Invoice> existing =
                invoiceRepository.findByRequestReference(
                        request.getRequestReference()
                );

        if (existing.isPresent()) {
            return mapInvoiceToResponse(existing.get());
        }

        Invoice invoice = Invoice.builder()
                .requestReference(request.getRequestReference())
                .organizationId(request.getOrganizationId())
                .branchId(request.getBranchId())
                .customerId(request.getCustomerId())
                .rentalOrderId(request.getRentalOrderId())
                .contractId(request.getRentalContractId())
                .invoiceType("RENTAL")
                .status(InvoiceStatus.DRAFT)
                .dueDate(request.getDueAt())
                .totalAmount(BigDecimal.ZERO)
                .build();

        invoice = invoiceRepository.save(invoice);

        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InternalInvoiceItemRequest itemRequest : request.getItems()) {

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .itemType(itemRequest.getType())
                    .description(itemRequest.getType())
                    .quantity(BigDecimal.ONE)
                    .unitPrice(itemRequest.getAmount())
                    .amount(itemRequest.getAmount())
                    .build();

            item = invoiceItemRepository.save(item);

            items.add(item);
            total = total.add(itemRequest.getAmount());
        }

        invoice.setItems(items);
        invoice.setTotalAmount(total);

        invoice = invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse addInternalCharge(
            Long invoiceId,
            InternalAddChargeRequest request
    ) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found: " + invoiceId
                        )
                );

        Optional<InvoiceItem> existing =
                invoiceItemRepository.findByRequestReference(
                        request.getRequestReference()
                );

        if (existing.isPresent()) {
            return mapInvoiceToResponse(invoice);
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot add charge to cancelled invoice"
            );
        }

        InvoiceItem item = InvoiceItem.builder()
                .invoice(invoice)
                .requestReference(
                        request.getRequestReference()
                )
                .itemType(
                        request.getChargeType()
                )
                .description(
                        request.getDescription()
                )
                .quantity(
                        BigDecimal.ONE
                )
                .unitPrice(
                        request.getAmount()
                )
                .amount(
                        request.getAmount()
                )
                .referenceType(
                        request.getReferenceType()
                )
                .referenceId(
                        request.getReferenceId()
                )
                .build();

        item = invoiceItemRepository.save(item);

        if (invoice.getItems() == null) {
            invoice.setItems(
                    new ArrayList<>()
            );
        }

        invoice.getItems().add(item);

        BigDecimal currentTotal =
                invoice.getTotalAmount() != null
                        ? invoice.getTotalAmount()
                        : BigDecimal.ZERO;

        invoice.setTotalAmount(
                currentTotal.add(
                        request.getAmount()
                )
        );

        invoice = invoiceRepository.save(invoice);

        return mapInvoiceToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InternalRentalOrderBillingStatusResponse
    getInternalRentalOrderBillingStatus(
            Long rentalOrderId
    ) {

        List<Invoice> invoices =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getRentalOrderId() != null
                                        && invoice.getRentalOrderId()
                                        .equals(rentalOrderId)
                                        && invoice.getStatus()
                                        != InvoiceStatus.CANCELLED
                        )
                        .toList();

        BigDecimal invoiceTotal =
                invoices.stream()
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<Long> invoiceIds =
                invoices.stream()
                        .map(Invoice::getId)
                        .toList();

        BigDecimal grossPaid =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                                        && payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refunded =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(
                                                payment.getId()
                                        )
                                        .stream()
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal paidAmount =
                grossPaid.subtract(refunded);

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            paidAmount = BigDecimal.ZERO;
        }

        BigDecimal outstandingAmount =
                invoiceTotal.subtract(paidAmount);

        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            outstandingAmount = BigDecimal.ZERO;
        }

        BigDecimal depositHeld =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getRentalOrderId() != null
                                        && deposit.getRentalOrderId()
                                        .equals(rentalOrderId)
                        )
                        .map(deposit -> {

                            BigDecimal amount =
                                    deposit.getAmount() != null
                                            ? deposit.getAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal deducted =
                                    deposit.getDeductedAmount() != null
                                            ? deposit.getDeductedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal refundedDeposit =
                                    deposit.getRefundedAmount() != null
                                            ? deposit.getRefundedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal remaining =
                                    amount
                                            .subtract(deducted)
                                            .subtract(refundedDeposit);

                            return remaining.compareTo(
                                    BigDecimal.ZERO
                            ) > 0
                                    ? remaining
                                    : BigDecimal.ZERO;
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        InternalRentalOrderBillingStatusResponse response =
                new InternalRentalOrderBillingStatusResponse();

        response.setRentalOrderId(rentalOrderId);
        response.setInvoiceTotal(invoiceTotal);
        response.setPaidAmount(paidAmount);
        response.setOutstandingAmount(outstandingAmount);
        response.setDepositHeld(depositHeld);

        response.setFullyPaid(
                invoiceTotal.compareTo(BigDecimal.ZERO) > 0
                        && outstandingAmount
                        .compareTo(BigDecimal.ZERO) == 0
        );

        return response;
    }

    @Transactional(readOnly = true)
    public InternalContractSettlementStatusResponse getInternalContractSettlementStatus(
            Long contractId
    ) {

        List<Invoice> invoices =
                invoiceRepository.findAll()
                        .stream()
                        .filter(invoice ->
                                invoice.getContractId() != null
                                        && invoice.getContractId().equals(contractId)
                                        && invoice.getStatus() != InvoiceStatus.CANCELLED
                        )
                        .toList();

        List<Long> invoiceIds =
                invoices.stream()
                        .map(Invoice::getId)
                        .toList();

        BigDecimal totalInvoiceAmount =
                invoices.stream()
                        .map(invoice ->
                                invoice.getTotalAmount() != null
                                        ? invoice.getTotalAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal grossPaid =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                                        && payment.getStatus() == PaymentStatus.SUCCESS
                        )
                        .map(payment ->
                                payment.getAmount() != null
                                        ? payment.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal refundedPayments =
                paymentRepository.findAll()
                        .stream()
                        .filter(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                        )
                        .flatMap(payment ->
                                paymentRefundRepository
                                        .findByPaymentId(payment.getId())
                                        .stream()
                        )
                        .map(refund ->
                                refund.getAmount() != null
                                        ? refund.getAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal netPaid =
                grossPaid.subtract(refundedPayments);

        if (netPaid.compareTo(BigDecimal.ZERO) < 0) {
            netPaid = BigDecimal.ZERO;
        }

        BigDecimal outstandingAmount =
                totalInvoiceAmount.subtract(netPaid);

        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            outstandingAmount = BigDecimal.ZERO;
        }

        BigDecimal depositRemaining =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getRentalContractId() != null
                                        && deposit.getRentalContractId()
                                        .equals(contractId)
                        )
                        .map(deposit -> {

                            BigDecimal amount =
                                    deposit.getAmount() != null
                                            ? deposit.getAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal deducted =
                                    deposit.getDeductedAmount() != null
                                            ? deposit.getDeductedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal refunded =
                                    deposit.getRefundedAmount() != null
                                            ? deposit.getRefundedAmount()
                                            : BigDecimal.ZERO;

                            BigDecimal remaining =
                                    amount
                                            .subtract(deducted)
                                            .subtract(refunded);

                            return remaining.compareTo(BigDecimal.ZERO) > 0
                                    ? remaining
                                    : BigDecimal.ZERO;
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        boolean hasUnresolvedPayment =
                paymentRepository.findAll()
                        .stream()
                        .anyMatch(payment ->
                                payment.getInvoice() != null
                                        && invoiceIds.contains(
                                        payment.getInvoice().getId()
                                )
                                        && payment.getStatus() == PaymentStatus.PENDING
                        );

        boolean canLiquidate =
                outstandingAmount.compareTo(BigDecimal.ZERO) == 0
                        && depositRemaining.compareTo(BigDecimal.ZERO) == 0
                        && !hasUnresolvedPayment;

        InternalContractSettlementStatusResponse response =
                new InternalContractSettlementStatusResponse();

        response.setContractId(contractId);
        response.setOutstandingAmount(outstandingAmount);
        response.setDepositRemaining(depositRemaining);
        response.setHasUnresolvedPayment(hasUnresolvedPayment);
        response.setCanLiquidate(canLiquidate);

        return response;
    }

    @Transactional(readOnly = true)
    public InternalCustomerDebtStatusResponse getInternalCustomerDebtStatus(
            Long customerId
    ) {

        LocalDateTime now = LocalDateTime.now();

        List<Debt> debts =
                debtRepository.findByCustomerId(customerId);

        BigDecimal totalOutstanding =
                debts.stream()
                        .map(debt ->
                                debt.getRemainingAmount() != null
                                        ? debt.getRemainingAmount()
                                        : BigDecimal.ZERO
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal overdueAmount =
                debts.stream()
                        .filter(debt ->
                                debt.getDueAt() != null
                                        && debt.getDueAt().isBefore(now)
                        )
                        .filter(debt ->
                                debt.getRemainingAmount() != null
                                        && debt.getRemainingAmount()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .map(Debt::getRemainingAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        InternalCustomerDebtStatusResponse response =
                new InternalCustomerDebtStatusResponse();

        response.setCustomerId(customerId);

        response.setTotalOutstanding(
                totalOutstanding
        );

        response.setOverdueAmount(
                overdueAmount
        );

        response.setHasDebt(
                totalOutstanding.compareTo(BigDecimal.ZERO) > 0
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponse> getInvoiceHistory(
            Long invoiceId
    ) {

        if (!invoiceRepository.existsById(invoiceId)) {
            throw new RuntimeException(
                    "Invoice not found: " + invoiceId
            );
        }

        return invoiceHistoryRepository
                .findByInvoiceIdOrderByCreatedAtAsc(invoiceId)
                .stream()
                .map(history -> {

                    InvoiceHistoryResponse response =
                            new InvoiceHistoryResponse();

                    response.setId(history.getId());
                    response.setInvoiceId(history.getInvoiceId());
                    response.setAction(history.getAction());
                    response.setOldStatus(history.getOldStatus());
                    response.setNewStatus(history.getNewStatus());
                    response.setDescription(history.getDescription());
                    response.setActorUserId(history.getActorUserId());
                    response.setCreatedAt(history.getCreatedAt());

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepositHistoryResponse> getDepositHistory(
            Long depositId
    ) {

        if (!depositRepository.existsById(depositId)) {
            throw new RuntimeException(
                    "Deposit not found: " + depositId
            );
        }

        return depositHistoryRepository
                .findByDepositIdOrderByCreatedAtAsc(depositId)
                .stream()
                .map(history -> {

                    DepositHistoryResponse response =
                            new DepositHistoryResponse();

                    response.setId(
                            history.getId()
                    );

                    response.setDepositId(
                            history.getDepositId()
                    );

                    response.setAction(
                            history.getAction()
                    );

                    response.setAmount(
                            history.getAmount()
                    );

                    response.setOldStatus(
                            history.getOldStatus()
                    );

                    response.setNewStatus(
                            history.getNewStatus()
                    );

                    response.setDescription(
                            history.getDescription()
                    );

                    response.setActorUserId(
                            history.getActorUserId()
                    );

                    response.setCreatedAt(
                            history.getCreatedAt()
                    );

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerBillingTransactionResponse> getCustomerTransactions(
            Long customerId
    ) {

        List<CustomerBillingTransactionResponse> transactions =
                new ArrayList<>();

        // =====================================================
        // 1. INVOICE
        // =====================================================

        invoiceRepository.findAll()
                .stream()
                .filter(invoice ->
                        invoice.getCustomerId() != null
                                && invoice.getCustomerId()
                                .equals(customerId)
                )
                .forEach(invoice -> {

                    CustomerBillingTransactionResponse response =
                            new CustomerBillingTransactionResponse();

                    response.setTransactionType("INVOICE");

                    response.setReferenceId(
                            invoice.getId()
                    );

                    response.setAmount(
                            invoice.getTotalAmount() != null
                                    ? invoice.getTotalAmount()
                                    : BigDecimal.ZERO
                    );

                    response.setStatus(
                            invoice.getStatus() != null
                                    ? invoice.getStatus().name()
                                    : null
                    );

                    response.setDescription(
                            "Hóa đơn "
                                    + (
                                    invoice.getInvoiceType() != null
                                            ? invoice.getInvoiceType()
                                            : ""
                            )
                    );

                    response.setOccurredAt(
                            invoice.getCreatedAt()
                    );

                    transactions.add(response);
                });

        // =====================================================
        // 2. DEPOSIT
        // =====================================================

        depositRepository.findAll()
                .stream()
                .filter(deposit ->
                        deposit.getCustomerId() != null
                                && deposit.getCustomerId()
                                .equals(customerId)
                )
                .forEach(deposit -> {

                    CustomerBillingTransactionResponse response =
                            new CustomerBillingTransactionResponse();

                    response.setTransactionType("DEPOSIT");

                    response.setReferenceId(
                            deposit.getId()
                    );

                    response.setAmount(
                            deposit.getAmount() != null
                                    ? deposit.getAmount()
                                    : BigDecimal.ZERO
                    );

                    response.setStatus(
                            deposit.getStatus()
                    );

                    response.setDescription(
                            "Tiền đặt cọc"
                    );

                    response.setOccurredAt(
                            deposit.getCreatedAt()
                    );

                    transactions.add(response);
                });

        // =====================================================
        // 3. PAYMENT
        // =====================================================

        paymentRepository.findAll()
                .stream()
                .filter(payment ->
                        payment.getInvoice() != null
                                && payment.getInvoice()
                                .getCustomerId() != null
                                && payment.getInvoice()
                                .getCustomerId()
                                .equals(customerId)
                )
                .forEach(payment -> {

                    CustomerBillingTransactionResponse response =
                            new CustomerBillingTransactionResponse();

                    response.setTransactionType("PAYMENT");

                    response.setReferenceId(
                            payment.getId()
                    );

                    response.setAmount(
                            payment.getAmount() != null
                                    ? payment.getAmount()
                                    : BigDecimal.ZERO
                    );

                    response.setStatus(
                            payment.getStatus() != null
                                    ? payment.getStatus().name()
                                    : null
                    );

                    response.setDescription(
                            "Thanh toán hóa đơn "
                                    + payment.getInvoice().getId()
                    );

                    LocalDateTime time =
                            payment.getConfirmationTime() != null
                                    ? payment.getConfirmationTime()
                                    : payment.getCreatedAt();

                    response.setOccurredAt(time);

                    transactions.add(response);
                });

        // =====================================================
        // 4. PAYMENT REFUND
        // =====================================================

        paymentRepository.findAll()
                .stream()
                .filter(payment ->
                        payment.getInvoice() != null
                                && payment.getInvoice()
                                .getCustomerId() != null
                                && payment.getInvoice()
                                .getCustomerId()
                                .equals(customerId)
                )
                .forEach(payment -> {

                    paymentRefundRepository
                            .findByPaymentId(payment.getId())
                            .forEach(refund -> {

                                CustomerBillingTransactionResponse response =
                                        new CustomerBillingTransactionResponse();

                                response.setTransactionType(
                                        "PAYMENT_REFUND"
                                );

                                response.setReferenceId(
                                        refund.getId()
                                );

                                response.setAmount(
                                        refund.getAmount() != null
                                                ? refund.getAmount()
                                                : BigDecimal.ZERO
                                );

                                response.setStatus(
                                        "REFUNDED"
                                );

                                response.setDescription(
                                        refund.getReason()
                                );

                                response.setOccurredAt(
                                        refund.getRefundedAt()
                                );

                                transactions.add(response);
                            });
                });

        // =====================================================
        // 5. DEBT
        // =====================================================

        debtRepository.findByCustomerId(customerId)
                .forEach(debt -> {

                    CustomerBillingTransactionResponse response =
                            new CustomerBillingTransactionResponse();

                    response.setTransactionType("DEBT");

                    response.setReferenceId(
                            debt.getId()
                    );

                    response.setAmount(
                            debt.getRemainingAmount() != null
                                    ? debt.getRemainingAmount()
                                    : BigDecimal.ZERO
                    );

                    response.setStatus(
                            debt.getStatus()
                    );

                    response.setDescription(
                            debt.getReason()
                    );

                    response.setOccurredAt(
                            debt.getCreatedAt()
                    );

                    transactions.add(response);
                });

        // =====================================================
        // 6. Sắp xếp mới nhất trước
        // =====================================================

        transactions.sort(
                (a, b) -> {

                    if (a.getOccurredAt() == null
                            && b.getOccurredAt() == null) {
                        return 0;
                    }

                    if (a.getOccurredAt() == null) {
                        return 1;
                    }

                    if (b.getOccurredAt() == null) {
                        return -1;
                    }

                    return b.getOccurredAt()
                            .compareTo(a.getOccurredAt());
                }
        );

        return transactions;
    }
}