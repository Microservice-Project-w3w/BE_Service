package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {}
