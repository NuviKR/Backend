package com.nuvi.nuvi.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
            @NotBlank
            @Size(min = 8, max = 2048)
            String code,
            @Size(min = 16, max = 256)
            String state,
            @NotBlank
            @Size(max = 2048)
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
            @NotBlank
            @Email
            String email,
            @NotBlank
            @Size(min = 8, max = 256)
            String password
    ) {
    }
}
