package com.equipmentrental.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Map<String, Object>>
    handleIdentityException(
        IdentityException exception
    ) {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                Map.of(
                    "timestamp",
                    LocalDateTime.now()
                        .toString(),
                    "message",
                    exception.getMessage()
                )
            );
    }
}
