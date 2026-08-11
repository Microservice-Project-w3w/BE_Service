package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.RestrictedCustomer;
import com.equipmentrental.organizationcustomer.enums.RestrictionStatus;
import com.equipmentrental.organizationcustomer.enums.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestrictedCustomerRepository
        extends JpaRepository<RestrictedCustomer, Long> {

    Optional<RestrictedCustomer>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<RestrictedCustomer>
    findAllByOrganizationIdOrderByIdDesc(
            Long organizationId
    );

    List<RestrictedCustomer>
    findAllByOrganizationIdAndCustomerIdOrderByIdDesc(
            Long organizationId,
            Long customerId
    );

    List<RestrictedCustomer>
    findAllByOrganizationIdAndCustomerIdAndStatus(
            Long organizationId,
            Long customerId,
            RestrictionStatus status
    );

    boolean
    existsByOrganizationIdAndCustomerIdAndRestrictionTypeAndStatus(
            Long organizationId,
            Long customerId,
            RestrictionType restrictionType,
            RestrictionStatus status
    );
}