package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.CustomerGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerGroupMemberRepository
        extends JpaRepository<CustomerGroupMember, Long> {

    boolean
    existsByOrganizationIdAndCustomerGroupIdAndCustomerId(
            Long organizationId,
            Long customerGroupId,
            Long customerId
    );

    List<CustomerGroupMember>
    findAllByOrganizationIdAndCustomerGroupIdOrderByIdDesc(
            Long organizationId,
            Long customerGroupId
    );

    Optional<CustomerGroupMember>
    findByOrganizationIdAndCustomerGroupIdAndCustomerId(
            Long organizationId,
            Long customerGroupId,
            Long customerId
    );
}