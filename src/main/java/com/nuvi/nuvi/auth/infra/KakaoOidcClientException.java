package com.nuvi.nuvi.auth.infra;

public class KakaoOidcClientException extends RuntimeException {

    public KakaoOidcClientException(String message) {
        super(message);
    }

    public KakaoOidcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
