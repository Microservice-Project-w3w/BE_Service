package com.equipmentrental.billing.service;

import com.equipmentrental.billing.entity.Debt;
import com.equipmentrental.billing.repository.DebtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;

    @Transactional
    public Debt addDebt(Long customerId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Debt amount must be greater than zero"
            );
        }

        List<Debt> debts =
                debtRepository.findByCustomerId(customerId);

        Debt debt;

        if (debts.isEmpty()) {

            debt = Debt.builder()
                    .organizationId(1L)
                    .branchId(1L)
                    .customerId(customerId)
                    .invoiceId(1L)
                    .amount(amount)
                    .remainingAmount(amount)
                    .status("OPEN")
                    .build();

        } else {

            debt = debts.stream()
                    .filter(item ->
                            !"SETTLED".equalsIgnoreCase(
                                    item.getStatus()
                            )
                    )
                    .findFirst()
                    .orElse(debts.get(0));

            BigDecimal currentAmount =
                    debt.getAmount() != null
                            ? debt.getAmount()
                            : BigDecimal.ZERO;

            BigDecimal currentRemaining =
                    debt.getRemainingAmount() != null
                            ? debt.getRemainingAmount()
                            : BigDecimal.ZERO;

            debt.setAmount(
                    currentAmount.add(amount)
            );

            debt.setRemainingAmount(
                    currentRemaining.add(amount)
            );

            debt.setStatus("OPEN");
        }

        return debtRepository.save(debt);
    }

    @Transactional
    public Debt reduceDebt(
            Long customerId,
            BigDecimal amount
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Reduction amount must be greater than zero"
            );
        }

        Debt debt = debtRepository
                .findByCustomerId(customerId)
                .stream()
                .filter(item ->
                        item.getRemainingAmount() != null
                                && item.getRemainingAmount()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Debt not found for customer: "
                                        + customerId
                        )
                );

        BigDecimal remainingAmount =
                debt.getRemainingAmount();

        if (remainingAmount.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Reduction amount exceeds current debt"
            );
        }

        BigDecimal newRemaining =
                remainingAmount.subtract(amount);

        debt.setRemainingAmount(newRemaining);

        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus("SETTLED");
        } else {
            debt.setStatus("PARTIALLY_SETTLED");
        }

        return debtRepository.save(debt);
    }

    @Transactional(readOnly = true)
    public List<Debt> getDebtsByCustomer(
            Long customerId
    ) {
        return debtRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDebt(
            Long customerId
    ) {

        return debtRepository
                .findByCustomerId(customerId)
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
    }
}