package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {
}
