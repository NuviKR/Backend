package com.nuvi.nuvi.auth.infra.adapter;

public interface KakaoOidcClient {

    KakaoOidcClaims exchangeCodeAndVerifyIdToken(String code, String redirectUri);
}
