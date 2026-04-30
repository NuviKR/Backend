package com.nuvi.nuvi.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "nuvi.auth.refresh-token")
public record RefreshTokenProperties(
        Duration ttl,
        String cookieName,
        String cookiePath,
        boolean cookieSecure,
        String sameSite
) {

    public RefreshTokenProperties {
        ttl = ttl == null ? Duration.ofDays(14) : ttl;
        cookieName = isBlank(cookieName) ? "nuvi_refresh_token" : cookieName;
        cookiePath = isBlank(cookiePath) ? "/api/v1/auth" : cookiePath;
        sameSite = isBlank(sameSite) ? "Lax" : sameSite;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
