package com.nuvi.nuvi.auth.infra.adapter;

import java.time.Instant;

public record KakaoOidcClaims(
        String subject,
        String issuer,
        String audience,
        Instant expiresAt
) {
}
