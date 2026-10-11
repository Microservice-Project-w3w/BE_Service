package com.equipmentrental.identity.service;

import com.equipmentrental.identity.entity.*;
import com.equipmentrental.identity.exception.IdentityException;
import com.equipmentrental.identity.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationTransactionPolicyTest {
    @Test void fiveWrongCodesDisableTheCode() {
        var repository = mock(VerificationCodeRepository.class);
        var encoder = mock(PasswordEncoder.class);
        var code = new VerificationCode(new User(), "demo@gmail.com", "RESET_PASSWORD", "hash", LocalDateTime.now().plusMinutes(15));
        when(repository.findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc("demo@gmail.com", "RESET_PASSWORD"))
                .thenReturn(Optional.of(code));
        var service = new VerificationService(repository, encoder, mock(EmailService.class), false);
        for (int i = 0; i < 5; i++)
            assertThrows(IdentityException.class, () -> service.verify("demo@gmail.com", "RESET_PASSWORD", "wrong"));
        assertFalse(code.canUse());
        assertThrows(IdentityException.class, () -> service.verify("demo@gmail.com", "RESET_PASSWORD", "right"));
        verify(encoder, times(5)).matches(anyString(), anyString());
    }
    @Test void failedOtpCountersAreNotRolledBackAtEitherTransactionBoundary() throws Exception {
        for (var method : List.of(
                VerificationService.class.getMethod("verify", String.class, String.class, String.class),
                AuthService.class.getMethod("verifyEmail", String.class, String.class),
                AuthService.class.getMethod("resetPassword", String.class, String.class, String.class))) {
            var policy = method.getAnnotation(Transactional.class);
            assertNotNull(policy);
            assertTrue(Arrays.asList(policy.noRollbackFor()).contains(IdentityException.class));
        }
    }
    @Test void consumableTokensAreReadUnderDatabaseWriteLocks() throws Exception {
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE,
                UserSessionRepository.class.getMethod("findByRefreshTokenHash", String.class).getAnnotation(Lock.class).value());
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE,
                VerificationCodeRepository.class.getMethod("findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc", String.class, String.class).getAnnotation(Lock.class).value());
    }
}
