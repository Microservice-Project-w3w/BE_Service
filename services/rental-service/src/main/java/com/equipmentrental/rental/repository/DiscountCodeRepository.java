package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
}
