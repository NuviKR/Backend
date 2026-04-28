package com.nuvi.nuvi.auth.application;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthSession;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthTokenResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoAuthorizeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthApplicationService {

    public AuthSession currentSession() {
        return new AuthSession(false, null, null, null, false);
    }

    public KakaoAuthorizeResponse createKakaoAuthorizationUrl(String redirectUri, String state) {
        String authorizeUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "nuvi-kakao-skeleton")
                .queryParam("redirect_uri", redirectUri)
                .queryParamIfPresent("state", java.util.Optional.ofNullable(state))
                .build()
                .toUriString();
        return new KakaoAuthorizeResponse(authorizeUrl);
    }

    public AuthTokenResponse completeKakaoLogin() {
        AuthSession session = new AuthSession(true, "mem_skeleton", AuthProvider.KAKAO, false, false);
        return new AuthTokenResponse("access_skeleton", null, 3600, session);
    }

    public AuthTokenResponse refreshToken() {
        AuthSession session = new AuthSession(true, "mem_skeleton", AuthProvider.KAKAO, false, false);
        return new AuthTokenResponse("access_skeleton_refreshed", null, 3600, session);
    }
}
