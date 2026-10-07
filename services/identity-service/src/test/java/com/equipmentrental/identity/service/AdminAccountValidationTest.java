package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.request.*;
import com.equipmentrental.identity.entity.UserStatus;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AdminAccountValidationTest {
    @Test void createRejectsNonGmailBeforeItReachesDatabaseConstraint() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var invalid = new AdminCreateUserRequest("Demo", "demo@example.com", "0901234567", "Demo@123", "SALES_STAFF", 1L, Set.of(1L), UserStatus.ACTIVE, true);
            assertFalse(factory.getValidator().validate(invalid).isEmpty());
        }
    }
    @Test void updateAcceptsGmailAndRejectsNonGmail() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new AdminUpdateUserRequest("Demo", "demo@gmail.com", "0901234567", "SALES_STAFF", 1L, Set.of(1L), UserStatus.ACTIVE)).isEmpty());
            assertFalse(validator.validate(new AdminUpdateUserRequest("Demo", "demo@example.com", "0901234567", "SALES_STAFF", 1L, Set.of(1L), UserStatus.ACTIVE)).isEmpty());
        }
    }
}
