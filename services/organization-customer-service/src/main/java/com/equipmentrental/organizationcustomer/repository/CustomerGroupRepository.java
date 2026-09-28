package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerGroupRepository
        extends JpaRepository<CustomerGroup, Long> {

    Optional<CustomerGroup>
    findByIdAndOrganizationIdAndDeletedAtIsNull(
            Long id,
            Long organizationId
    );

    List<CustomerGroup>
    findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(
            Long organizationId
    );

    boolean
    existsByOrganizationIdAndGroupCodeAndDeletedAtIsNull(
            Long organizationId,
            String groupCode
    );
}