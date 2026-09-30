package com.equipmentrental.identity.service;

import com.equipmentrental.identity.exception.IdentityException;
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
    private final EmailService emailService;
    private final boolean exposeCode;


    public VerificationService(
        VerificationCodeRepository repository,
        PasswordEncoder passwordEncoder,
        EmailService emailService,
        @Value("${app.auth.expose-verification-code:false}") boolean exposeCode) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.exposeCode = exposeCode;
    }

    public String issue(User user, String purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        repository.save(new VerificationCode(
            user,
            user.getEmail(),
            purpose,
            passwordEncoder.encode(code),
            LocalDateTime.now().plusMinutes(15)
        ));

        emailService.sendVerificationCode(user.getEmail(), code, purpose);

        return exposeCode ? code : null;
    }

    public User verify(String email, String purpose, String code) {
        VerificationCode verification = repository.findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(email, purpose)
            .orElseThrow(() ->
                new IdentityException(
                    "Mã xác minh không tồn tại"
                )
            );
        if (!verification.canUse()) {
            throw new IdentityException(
                "Mã xác minh đã hết hạn hoặc vượt quá số lần thử"
            );
        }
        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            verification.recordAttempt();
            throw new IdentityException(
                "Mã xác minh không chính xác"
            );
        }
        verification.markUsed();
        if (verification.getUser() == null) {
            throw new IdentityException(
                "Không tìm thấy tài khoản xác minh"
            );
        }
        return verification.getUser();
    }
}
