package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer>
    findByIdAndOrganizationIdAndDeletedAtIsNull(
            Long id,
            Long organizationId
    );

    boolean
    existsByOrganizationIdAndCustomerCodeAndDeletedAtIsNull(
            Long organizationId,
            String customerCode
    );
}