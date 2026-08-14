package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositHistoryRepository
        extends JpaRepository<DepositHistory, Long> {

    List<DepositHistory> findByDepositIdOrderByCreatedAtAsc(
            Long depositId
    );
}