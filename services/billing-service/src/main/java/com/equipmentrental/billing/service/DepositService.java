package com.equipmentrental.billing.service;

import com.equipmentrental.billing.dto.request.CreateDepositRequest;
import com.equipmentrental.billing.dto.response.DepositResponse;
import com.equipmentrental.billing.entity.Deposit;
import com.equipmentrental.billing.entity.DepositTransaction;
import com.equipmentrental.billing.entity.enums.DepositTransactionType;
import com.equipmentrental.billing.entity.enums.PaymentStatus;
import com.equipmentrental.billing.repository.DepositRepository;
import com.equipmentrental.billing.repository.DepositTransactionRepository;
import com.equipmentrental.billing.dto.request.CreateDepositDeductionRequest;
import com.equipmentrental.billing.dto.response.DepositDeductionResponse;
import com.equipmentrental.billing.dto.request.RefundDepositRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositTransactionRepository depositTransactionRepository;

    @Transactional
    public DepositResponse createDeposit(
            CreateDepositRequest request
    ) {

        Deposit deposit = new Deposit();

        deposit.setOrganizationId(request.getOrganizationId());
        deposit.setBranchId(request.getBranchId());
        deposit.setCustomerId(request.getCustomerId());
        deposit.setRentalOrderId(request.getRentalOrderId());
        deposit.setRentalContractId(request.getRentalContractId());
        deposit.setAmount(request.getAmount());
        deposit.setDeductedAmount(BigDecimal.ZERO);
        deposit.setRefundedAmount(BigDecimal.ZERO);
        deposit.setPaymentMethod(request.getPaymentMethod());
        deposit.setReference(request.getReference());
        deposit.setNotes(request.getNotes());
        deposit.setStatus("HELD");

        deposit = depositRepository.save(deposit);

        // Ghi lịch sử thu tiền cọc
        DepositTransaction transaction =
                DepositTransaction.builder()
                        .contractId(request.getRentalContractId())
                        .transactionType(
                                DepositTransactionType.COLLECTION
                        )
                        .amount(request.getAmount())
                        .status(PaymentStatus.SUCCESS)
                        .referenceId(
                                request.getReference()
                        )
                        .build();

        depositTransactionRepository.save(transaction);

        return mapToResponse(deposit);
    }

    private DepositResponse mapToResponse(
            Deposit deposit
    ) {

        DepositResponse response =
                new DepositResponse();

        response.setId(deposit.getId());

        response.setOrganizationId(
                deposit.getOrganizationId()
        );

        response.setBranchId(
                deposit.getBranchId()
        );

        response.setCustomerId(
                deposit.getCustomerId()
        );

        response.setRentalOrderId(
                deposit.getRentalOrderId()
        );

        response.setRentalContractId(
                deposit.getRentalContractId()
        );

        response.setAmount(
                deposit.getAmount()
        );

        response.setDeductedAmount(
                deposit.getDeductedAmount()
        );

        response.setRefundedAmount(
                deposit.getRefundedAmount()
        );

        response.setRemainingAmount(
                deposit.getAmount()
                        .subtract(
                                deposit.getDeductedAmount()
                        )
                        .subtract(
                                deposit.getRefundedAmount()
                        )
        );

        response.setPaymentMethod(
                deposit.getPaymentMethod()
        );

        response.setReference(
                deposit.getReference()
        );

        response.setNotes(
                deposit.getNotes()
        );

        response.setStatus(
                deposit.getStatus()
        );

        response.setCreatedAt(
                deposit.getCreatedAt()
        );

        response.setUpdatedAt(
                deposit.getUpdatedAt()
        );

        return response;
    }
    @Transactional(readOnly = true)
    public List<DepositResponse> getAllDeposits() {
        return depositRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepositResponse getDepositById(Long id) {

        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Deposit not found: " + id
                        )
                );

        return mapToResponse(deposit);
    }

    @Transactional
    public DepositResponse deductDeposit(
            Long id,
            CreateDepositDeductionRequest request
    ) {

        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Deposit not found: " + id
                        )
                );

        BigDecimal remainingAmount =
                deposit.getAmount()
                        .subtract(deposit.getDeductedAmount())
                        .subtract(deposit.getRefundedAmount());

        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Deduction amount exceeds remaining deposit"
            );
        }

        deposit.setDeductedAmount(
                deposit.getDeductedAmount()
                        .add(request.getAmount())
        );

        if (deposit.getDeductedAmount().compareTo(BigDecimal.ZERO) > 0) {
            deposit.setStatus("PARTIALLY_DEDUCTED");
        }

        deposit = depositRepository.save(deposit);

        DepositTransaction transaction = new DepositTransaction();

        transaction.setContractId(
                deposit.getRentalContractId()
        );

        transaction.setTransactionType(
                DepositTransactionType.DEDUCTION
        );

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setStatus(
                PaymentStatus.SUCCESS
        );

        transaction.setReferenceId(
                request.getReferenceType() + "-" + request.getReferenceId()
        );

        depositTransactionRepository.save(transaction);

        return mapToResponse(deposit);
    }
    @Transactional(readOnly = true)
    public List<DepositDeductionResponse> getDeductions(Long depositId) {

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Deposit not found: " + depositId
                        )
                );

        return depositTransactionRepository
                .findByContractIdAndTransactionType(
                        deposit.getRentalContractId(),
                        DepositTransactionType.DEDUCTION
                )
                .stream()
                .map(transaction -> {

                    DepositDeductionResponse response =
                            new DepositDeductionResponse();

                    response.setId(transaction.getId());
                    response.setAmount(transaction.getAmount());
                    response.setReference(transaction.getReferenceId());
                    response.setCreatedAt(transaction.getCreatedAt());

                    return response;
                })
                .toList();
    }
    @Transactional
    public DepositResponse refundDeposit(
            Long id,
            RefundDepositRequest request
    ) {

        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Deposit not found: " + id
                        )
                );

        BigDecimal remainingAmount =
                deposit.getAmount()
                        .subtract(deposit.getDeductedAmount())
                        .subtract(deposit.getRefundedAmount());

        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount exceeds remaining deposit"
            );
        }

        deposit.setRefundedAmount(
                deposit.getRefundedAmount()
                        .add(request.getAmount())
        );

        BigDecimal newRemaining =
                deposit.getAmount()
                        .subtract(deposit.getDeductedAmount())
                        .subtract(deposit.getRefundedAmount());

        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            deposit.setStatus("REFUNDED");
        } else {
            deposit.setStatus("PARTIALLY_DEDUCTED");
        }

        deposit = depositRepository.save(deposit);

        DepositTransaction transaction = new DepositTransaction();

        transaction.setContractId(
                deposit.getRentalContractId()
        );

        transaction.setTransactionType(
                DepositTransactionType.REFUND
        );

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setStatus(
                PaymentStatus.SUCCESS
        );

        transaction.setReferenceId(
                "REFUND-DEPOSIT-" + deposit.getId()
        );

        depositTransactionRepository.save(transaction);

        return mapToResponse(deposit);
    }
}