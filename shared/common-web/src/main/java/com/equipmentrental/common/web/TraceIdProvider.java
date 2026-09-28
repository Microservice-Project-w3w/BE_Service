package com.equipmentrental.common.web;

import org.slf4j.MDC;

public class TraceIdProvider {
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    public String getTraceId() {
        return currentTraceId();
    }

    public static String currentTraceId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
}
