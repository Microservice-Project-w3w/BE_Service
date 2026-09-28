package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    Optional<Organization>
    findByIdAndDeletedAtIsNull(Long id);

    List<Organization>
    findAllByDeletedAtIsNullOrderByIdDesc();

    boolean
    existsByOrganizationCodeAndDeletedAtIsNull(
            String organizationCode
    );

    boolean
    existsByTaxCodeAndDeletedAtIsNull(
            String taxCode
    );
}