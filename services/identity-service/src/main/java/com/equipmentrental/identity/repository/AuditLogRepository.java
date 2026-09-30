package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @EntityGraph(attributePaths = "user")
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "user")
    List<AuditLog> findTop200ByUserIdOrderByCreatedAtDesc(Long userId);
}
