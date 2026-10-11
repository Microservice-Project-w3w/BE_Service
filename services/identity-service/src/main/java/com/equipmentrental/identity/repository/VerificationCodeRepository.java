package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.VerificationCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(String email, String purpose);
}
