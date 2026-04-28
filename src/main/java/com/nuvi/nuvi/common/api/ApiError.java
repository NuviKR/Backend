package com.nuvi.nuvi.common.api;

import java.util.List;

public record ApiError(
        ApiErrorCode code,
        String message,
        List<ErrorDetail> details
) {

    public ApiError {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ApiError of(ApiErrorCode code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(ApiErrorCode code, String message, List<ErrorDetail> details) {
        return new ApiError(code, message, details);
    }
}
