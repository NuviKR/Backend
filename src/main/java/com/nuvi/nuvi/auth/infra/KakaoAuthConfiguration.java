package com.nuvi.nuvi.auth.infra;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KakaoAuthProperties.class)
class KakaoAuthConfiguration {
}
