package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/** Sales records an external customer decision; this does not impersonate customer login. */
public record CustomerConfirmationRequest(@NotNull @AssertTrue Boolean customerConfirmed) {}
