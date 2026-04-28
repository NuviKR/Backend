package com.nuvi.nuvi.common.api;

public record ErrorResponse(
        ApiError error,
        Meta meta
) {

    public static ErrorResponse of(ApiError error, Meta meta) {
        return new ErrorResponse(error, meta);
    }
}
