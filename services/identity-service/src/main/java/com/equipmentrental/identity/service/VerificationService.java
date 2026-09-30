package com.equipmentrental.identity.service;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.VerificationCode;
import com.equipmentrental.identity.repository.VerificationCodeRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VerificationService {
    public static final String PURPOSE_VERIFY_EMAIL = "VERIFY_EMAIL";
    public static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final VerificationCodeRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final boolean exposeCode;


    public VerificationService(VerificationCodeRepository repository, PasswordEncoder passwordEncoder,
                               @Value("${app.auth.expose-verification-code:false}") boolean exposeCode) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.exposeCode = exposeCode;
    }

    public String issue(User user, String purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        repository.save(new VerificationCode(user, user.getEmail(), purpose, passwordEncoder.encode(code),
                LocalDateTime.now().plusMinutes(15)));
        // Email provider will consume this code in a later integration. It is exposed only when explicitly enabled for local testing.
        return exposeCode ? code : null;
    }

    public User verify(String email, String purpose, String code) {
        VerificationCode verification = repository.findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.AUTH_TOKEN_INVALID, "Mã xác minh không tồn tại"));
        if (!verification.canUse()) {
            throw new BusinessException(CommonErrorCode.AUTH_TOKEN_EXPIRED, "Mã xác minh đã hết hạn hoặc vượt quá số lần thử");
        }
        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            verification.recordAttempt();
            throw new BusinessException(CommonErrorCode.AUTH_TOKEN_INVALID, "Mã xác minh không chính xác");
        }
        verification.markUsed();
        if (verification.getUser() == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản xác minh");
        }
        return verification.getUser();
    }
}
