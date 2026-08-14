package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.InvoiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceHistoryRepository
        extends JpaRepository<InvoiceHistory, Long> {

    List<InvoiceHistory> findByInvoiceIdOrderByCreatedAtAsc(
            Long invoiceId
    );
}