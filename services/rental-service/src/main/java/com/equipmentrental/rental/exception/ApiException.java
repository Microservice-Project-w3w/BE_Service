package com.equipmentrental.rental.exception;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;

public class ApiException extends BusinessException {
    public ApiException(String message) {
        super(CommonErrorCode.VALIDATION_FAILED, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ApiException invalidStatus(String message) {
        return new ApiException(CommonErrorCode.INVALID_STATUS_TRANSITION, message);
    }

    private ApiException(CommonErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
