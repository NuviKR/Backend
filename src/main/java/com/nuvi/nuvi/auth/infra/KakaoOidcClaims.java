package com.nuvi.nuvi.auth.infra;

import java.time.Instant;

public record KakaoOidcClaims(
        String subject,
        String issuer,
        String audience,
        Instant expiresAt
) {
}
