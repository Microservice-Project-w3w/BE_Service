package com.equipmentrental.common.web;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String code();

    HttpStatus status();

    String defaultMessage();
}
