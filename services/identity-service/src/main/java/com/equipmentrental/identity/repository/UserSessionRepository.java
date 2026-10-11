package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.UserSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    List<UserSession> findByUserIdOrderByLoginAtDesc(Long userId);
    Optional<UserSession> findByIdAndUserId(Long id, Long userId);
}
