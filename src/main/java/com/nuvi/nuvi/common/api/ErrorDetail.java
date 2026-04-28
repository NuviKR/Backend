package com.nuvi.nuvi.common.api;

public record ErrorDetail(
        String field,
        String reason
) {
}
