package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, Long> {
    Optional<InvoiceItem> findByRequestReference(
            String requestReference
    );
}