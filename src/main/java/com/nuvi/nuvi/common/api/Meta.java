package com.nuvi.nuvi.common.api;

import java.time.Instant;

public record Meta(
        String requestId,
        Instant serverTime
) {
}
