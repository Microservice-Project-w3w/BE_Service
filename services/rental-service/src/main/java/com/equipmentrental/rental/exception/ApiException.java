package com.equipmentrental.rental.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
