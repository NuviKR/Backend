package com.nuvi.nuvi.common.api;

import java.util.Map;

public record ApiResponse<T>(
        T data,
        Meta meta
) {

    public static <T> ApiResponse<T> ok(T data, Meta meta) {
        return new ApiResponse<>(data, meta);
    }

    public static ApiResponse<Map<String, Object>> empty(Meta meta) {
        return new ApiResponse<>(Map.of(), meta);
    }
}
