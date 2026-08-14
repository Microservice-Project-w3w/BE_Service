package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.DeliveryFeeRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryFeeRuleRepository extends JpaRepository<DeliveryFeeRule, Long> {
    List<DeliveryFeeRule> findByIsActiveTrue();
}
