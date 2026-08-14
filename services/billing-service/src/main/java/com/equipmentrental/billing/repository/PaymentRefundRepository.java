package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRefundRepository
        extends JpaRepository<PaymentRefund, Long> {

    List<PaymentRefund> findByPaymentId(Long paymentId);
}