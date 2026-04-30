package com.nuvi.nuvi.auth.controller;

import com.nuvi.nuvi.auth.application.AuthApplicationService;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthSession;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthTokenResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.EmailLoginRequest;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoAuthorizeResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoCallbackRequest;
import com.nuvi.nuvi.auth.infra.config.RefreshTokenProperties;
import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import com.nuvi.nuvi.common.api.ApiResponse;
import com.nuvi.nuvi.common.api.RequestMetaFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authService;
    private final RequestMetaFactory metaFactory;
    private final RefreshTokenProperties refreshTokenProperties;

    public AuthController(
            AuthApplicationService authService,
            RequestMetaFactory metaFactory,
            RefreshTokenProperties refreshTokenProperties
    ) {
        this.authService = authService;
        this.metaFactory = metaFactory;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    @GetMapping("/session")
    public ApiResponse<AuthSession> getCurrentSession() {
        return ApiResponse.ok(authService.currentSession(), metaFactory.current());
    }

    @GetMapping("/kakao/authorize")
    public ApiResponse<KakaoAuthorizeResponse> createKakaoAuthorizationUrl(
            @RequestParam @NotBlank @Size(max = 2048) String redirectUri,
            @RequestParam(required = false) @Size(min = 16, max = 256) String state
    ) {
        return ApiResponse.ok(authService.createKakaoAuthorizationUrl(redirectUri, state), metaFactory.current());
    }

    @PostMapping("/kakao/callback")
    public ApiResponse<AuthTokenResponse> completeKakaoLogin(
            @Valid @RequestBody KakaoCallbackRequest request,
            HttpServletResponse response
    ) {
        AuthApplicationService.AuthTokenIssue issue = authService.completeKakaoLogin(request);
        writeRefreshTokenCookie(response, issue.refreshToken());
        return ApiResponse.ok(issue.response(), metaFactory.current());
    }

    @PostMapping("/email/login")
    public ApiResponse<Map<String, Object>> loginWithEmail(@Valid @RequestBody EmailLoginRequest request) {
        throw new ApiException(
                HttpStatus.FORBIDDEN,
                ApiErrorCode.EMAIL_AUTH_DISABLED,
                ApiErrorCode.EMAIL_AUTH_DISABLED.defaultMessage()
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refreshSessionToken(HttpServletRequest request, HttpServletResponse response) {
        AuthApplicationService.AuthTokenIssue issue = authService.refreshToken(readRefreshTokenCookie(request));
        writeRefreshTokenCookie(response, issue.refreshToken());
        return ApiResponse.ok(issue.response(), metaFactory.current());
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logoutCurrentSession(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(readRefreshTokenCookie(request));
        clearRefreshTokenCookie(response);
        return ApiResponse.empty(metaFactory.current());
    }

    private String readRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (refreshTokenProperties.cookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(refreshTokenProperties.cookieName(), refreshToken)
                .httpOnly(true)
                .secure(refreshTokenProperties.cookieSecure())
                .sameSite(refreshTokenProperties.sameSite())
                .path(refreshTokenProperties.cookiePath())
                .maxAge(refreshTokenProperties.ttl())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshTokenProperties.cookieName(), "")
                .httpOnly(true)
                .secure(refreshTokenProperties.cookieSecure())
                .sameSite(refreshTokenProperties.sameSite())
                .path(refreshTokenProperties.cookiePath())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
