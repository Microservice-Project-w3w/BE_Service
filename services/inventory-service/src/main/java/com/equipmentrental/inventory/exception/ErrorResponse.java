package com.equipmentrental.inventory.exception;


public record ErrorResponse(
        int status,
        String message
) {}