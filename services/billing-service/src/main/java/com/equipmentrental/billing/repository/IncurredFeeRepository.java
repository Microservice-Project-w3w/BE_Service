package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.IncurredFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncurredFeeRepository extends JpaRepository<IncurredFee, Long> {
    List<IncurredFee> findByInvoiceId(Long invoiceId);
}
