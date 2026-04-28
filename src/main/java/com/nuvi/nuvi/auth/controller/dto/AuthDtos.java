package com.nuvi.nuvi.auth.controller.dto;

public final class AuthDtos {

    private AuthDtos() {
    }

    public enum AuthProvider {
        KAKAO
    }

    public record AuthSession(
            boolean authenticated,
            String memberId,
            AuthProvider provider,
            Boolean onboardingCompleted,
            boolean emailAuthEnabled
    ) {
    }

    public record KakaoAuthorizeResponse(
            String authorizeUrl
    ) {
    }

    public record KakaoCallbackRequest(
            String code,
            String state,
            String redirectUri
    ) {
    }

    public record AuthTokenResponse(
            String accessToken,
            String refreshToken,
            int expiresInSeconds,
            AuthSession session
    ) {
    }

    public record EmailLoginRequest(
            String email,
            String password
    ) {
    }
}
