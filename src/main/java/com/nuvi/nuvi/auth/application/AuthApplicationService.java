package com.nuvi.nuvi.auth.application;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthSession;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthTokenResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoAuthorizeResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoCallbackRequest;
import com.nuvi.nuvi.auth.domain.MemberIdentity;
import com.nuvi.nuvi.auth.domain.OidcMemberRepository;
import com.nuvi.nuvi.auth.infra.KakaoAuthProperties;
import com.nuvi.nuvi.auth.infra.KakaoOidcClaims;
import com.nuvi.nuvi.auth.infra.KakaoOidcClient;
import com.nuvi.nuvi.auth.infra.KakaoOidcClientException;
import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthApplicationService {

    private static final String KAKAO_AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String KAKAO_SCOPE = "openid";

    private final KakaoAuthProperties kakaoProperties;
    private final KakaoOidcClient kakaoOidcClient;
    private final OidcMemberRepository memberRepository;

    public AuthApplicationService(
            KakaoAuthProperties kakaoProperties,
            KakaoOidcClient kakaoOidcClient,
            OidcMemberRepository memberRepository
    ) {
        this.kakaoProperties = kakaoProperties;
        this.kakaoOidcClient = kakaoOidcClient;
        this.memberRepository = memberRepository;
    }

    public AuthSession currentSession() {
        return new AuthSession(false, null, null, null, false);
    }

    public KakaoAuthorizeResponse createKakaoAuthorizationUrl(String redirectUri, String state) {
        String clientId = kakaoProperties.restApiKey();
        if (clientId == null || clientId.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR, "Kakao OAuth is not configured.");
        }

        String authorizeUrl = UriComponentsBuilder
                .fromUriString(KAKAO_AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", KAKAO_SCOPE)
                .queryParamIfPresent("state", java.util.Optional.ofNullable(state))
                .build()
                .toUriString();
        return new KakaoAuthorizeResponse(authorizeUrl);
    }

    public AuthTokenResponse completeKakaoLogin(KakaoCallbackRequest request) {
        KakaoOidcClaims claims;
        try {
            claims = kakaoOidcClient.exchangeCodeAndVerifyIdToken(request.code(), request.redirectUri());
        } catch (KakaoOidcClientException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, "Kakao OIDC authentication failed.");
        }
        MemberIdentity member = memberRepository.findOrCreate(AuthProvider.KAKAO, claims.subject());
        AuthSession session = new AuthSession(true, member.memberId(), AuthProvider.KAKAO, false, false);
        return new AuthTokenResponse("access_skeleton", null, 3600, session);
    }

    public AuthTokenResponse refreshToken() {
        AuthSession session = new AuthSession(true, "mem_skeleton", AuthProvider.KAKAO, false, false);
        return new AuthTokenResponse("access_skeleton_refreshed", null, 3600, session);
    }
}
