package com.nuvi.nuvi.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nuvi.auth.kakao")
public record KakaoAuthProperties(
        String restApiKey,
        String clientSecret,
        String tokenUri,
        String issuer,
        String jwksUri
) {
}
