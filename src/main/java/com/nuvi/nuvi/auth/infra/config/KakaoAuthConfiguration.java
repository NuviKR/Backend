package com.nuvi.nuvi.auth.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KakaoAuthProperties.class, RefreshTokenProperties.class})
class KakaoAuthConfiguration {
}
