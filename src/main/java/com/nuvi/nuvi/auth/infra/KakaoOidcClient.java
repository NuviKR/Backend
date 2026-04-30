package com.nuvi.nuvi.auth.infra;

public interface KakaoOidcClient {

    KakaoOidcClaims exchangeCodeAndVerifyIdToken(String code, String redirectUri);
}
