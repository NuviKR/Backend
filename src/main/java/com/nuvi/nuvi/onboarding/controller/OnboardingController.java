package com.nuvi.nuvi.onboarding.controller;

import com.nuvi.nuvi.common.api.ApiResponse;
import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import com.nuvi.nuvi.common.api.RequestMetaFactory;
import com.nuvi.nuvi.onboarding.application.OnboardingApplicationService;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingCompletionResponse;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileCreateRequest;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfilePatchRequest;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingApplicationService onboardingService;
    private final RequestMetaFactory metaFactory;

    public OnboardingController(OnboardingApplicationService onboardingService, RequestMetaFactory metaFactory) {
        this.onboardingService = onboardingService;
        this.metaFactory = metaFactory;
    }

    @GetMapping("/profile")
    public ApiResponse<OnboardingProfileResponse> getOnboardingProfile(
            @RequestHeader(name = "X-Nuvi-Member-Id", required = false) String memberId
    ) {
        return ApiResponse.ok(onboardingService.getProfile(requireMemberId(memberId)), metaFactory.current());
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<OnboardingProfileResponse>> createOnboardingProfile(
            @RequestHeader(name = "X-Nuvi-Member-Id", required = false) String memberId,
            @Valid @RequestBody OnboardingProfileCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(onboardingService.createProfile(requireMemberId(memberId), request), metaFactory.current()));
    }

    @PatchMapping("/profile")
    public ApiResponse<OnboardingProfileResponse> patchOnboardingProfile(
            @RequestHeader(name = "X-Nuvi-Member-Id", required = false) String memberId,
            @Valid @RequestBody OnboardingProfilePatchRequest request
    ) {
        return ApiResponse.ok(onboardingService.patchProfile(requireMemberId(memberId), request), metaFactory.current());
    }

    @PostMapping("/complete")
    public ApiResponse<OnboardingCompletionResponse> completeOnboarding(
            @RequestHeader(name = "X-Nuvi-Member-Id", required = false) String memberId
    ) {
        return ApiResponse.ok(onboardingService.completeOnboarding(requireMemberId(memberId)), metaFactory.current());
    }

    private static String requireMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTH_REQUIRED);
        }
        return memberId;
    }
}
