package com.equipmentrental.rental.dto;

import com.equipmentrental.rental.dto.request.CustomerConfirmationRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerConfirmationRequestTest {
    @Test void requiresExplicitPositiveCustomerConfirmation() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validate(new CustomerConfirmationRequest(null)).isEmpty());
            assertFalse(validator.validate(new CustomerConfirmationRequest(false)).isEmpty());
            assertTrue(validator.validate(new CustomerConfirmationRequest(true)).isEmpty());
        }
    }
}
