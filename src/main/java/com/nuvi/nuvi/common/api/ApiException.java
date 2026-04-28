package com.nuvi.nuvi.common.api;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ApiErrorCode code;
    private final List<ErrorDetail> details;

    public ApiException(HttpStatus status, ApiErrorCode code) {
        this(status, code, code.defaultMessage(), List.of());
    }

    public ApiException(HttpStatus status, ApiErrorCode code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(HttpStatus status, ApiErrorCode code, String message, List<ErrorDetail> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus status() {
        return status;
    }

    public ApiErrorCode code() {
        return code;
    }

    public List<ErrorDetail> details() {
        return details;
    }
}
