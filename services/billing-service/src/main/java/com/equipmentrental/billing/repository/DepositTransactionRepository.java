package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.DepositTransaction;
import com.equipmentrental.billing.entity.enums.DepositTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositTransactionRepository
        extends JpaRepository<DepositTransaction, Long> {

    List<DepositTransaction> findByContractIdAndTransactionType(
            Long contractId,
            DepositTransactionType transactionType
    );
}