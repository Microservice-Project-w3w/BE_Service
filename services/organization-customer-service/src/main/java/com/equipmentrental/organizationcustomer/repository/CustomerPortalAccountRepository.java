package com.equipmentrental.organizationcustomer.repository;

import com.equipmentrental.organizationcustomer.entity.CustomerPortalAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalAccountRepository extends JpaRepository<CustomerPortalAccount, Long> {
    Optional<CustomerPortalAccount> findByCustomerId(Long customerId);
}
