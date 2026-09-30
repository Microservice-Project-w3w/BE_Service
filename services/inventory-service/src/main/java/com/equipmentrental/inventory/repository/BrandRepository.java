package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository
        extends JpaRepository<Brand, Long> {

    List<Brand> findByOrganizationId(
            Long organizationId
    );

    Optional<Brand> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<Brand> findByOrganizationIdAndCode(
            Long organizationId,
            String code
    );

    boolean existsByOrganizationIdAndCode(
            Long organizationId,
            String code
    );
}