package com.nuvi.nuvi.onboarding.controller;

import com.nuvi.nuvi.common.api.ApiResponse;
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
    public ApiResponse<OnboardingProfileResponse> getOnboardingProfile() {
        return ApiResponse.ok(onboardingService.getProfile(), metaFactory.current());
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<OnboardingProfileResponse>> createOnboardingProfile(
            @Valid @RequestBody OnboardingProfileCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(onboardingService.createProfile(request), metaFactory.current()));
    }

    @PatchMapping("/profile")
    public ApiResponse<OnboardingProfileResponse> patchOnboardingProfile(@Valid @RequestBody OnboardingProfilePatchRequest request) {
        return ApiResponse.ok(onboardingService.patchProfile(request), metaFactory.current());
    }

    @PostMapping("/complete")
    public ApiResponse<OnboardingCompletionResponse> completeOnboarding() {
        return ApiResponse.ok(onboardingService.completeOnboarding(), metaFactory.current());
    }
}
