package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositRepository
        extends JpaRepository<Deposit, Long> {
}