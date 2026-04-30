package com.nuvi.nuvi.auth.controller;

import com.nuvi.nuvi.auth.application.AuthApplicationService;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthSession;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthTokenResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.EmailLoginRequest;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoAuthorizeResponse;
import com.nuvi.nuvi.auth.controller.dto.AuthDtos.KakaoCallbackRequest;
import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import com.nuvi.nuvi.common.api.ApiResponse;
import com.nuvi.nuvi.common.api.RequestMetaFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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

    public AuthController(AuthApplicationService authService, RequestMetaFactory metaFactory) {
        this.authService = authService;
        this.metaFactory = metaFactory;
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
    public ApiResponse<AuthTokenResponse> completeKakaoLogin(@Valid @RequestBody KakaoCallbackRequest request) {
        return ApiResponse.ok(authService.completeKakaoLogin(request), metaFactory.current());
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
    public ApiResponse<AuthTokenResponse> refreshSessionToken() {
        return ApiResponse.ok(authService.refreshToken(), metaFactory.current());
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logoutCurrentSession() {
        return ApiResponse.empty(metaFactory.current());
    }
}
