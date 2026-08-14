package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByRentalOrderId(Long rentalOrderId);
    Optional<Invoice> findByContractId(Long contractId);
    Optional<Invoice> findByRequestReference(String requestReference);
}
