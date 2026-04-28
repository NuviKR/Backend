package com.nuvi.nuvi.onboarding.application;

import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.AgeRange;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.FoodPreferenceInput;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.HealthGoalCode;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.Money;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingCompletionResponse;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileRequest;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileResponse;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingStatus;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.SupplementContextInput;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OnboardingApplicationService {

    public OnboardingProfileResponse getProfile() {
        return skeletonProfile(OnboardingStatus.DRAFT);
    }

    public OnboardingProfileResponse createProfile(OnboardingProfileRequest request) {
        return fromRequest(request, OnboardingStatus.DRAFT);
    }

    public OnboardingProfileResponse patchProfile(OnboardingProfileRequest request) {
        return fromRequest(request, OnboardingStatus.DRAFT);
    }

    public OnboardingCompletionResponse completeOnboarding() {
        return new OnboardingCompletionResponse(true, "GENERATE_WEEKLY_CART");
    }

    private OnboardingProfileResponse fromRequest(OnboardingProfileRequest request, OnboardingStatus status) {
        OnboardingProfileResponse fallback = skeletonProfile(status);
        return new OnboardingProfileResponse(
                request.ageRange() == null ? fallback.ageRange() : request.ageRange(),
                request.householdSize() == null ? fallback.householdSize() : request.householdSize(),
                request.weeklyBudget() == null ? fallback.weeklyBudget() : request.weeklyBudget(),
                request.goals() == null ? fallback.goals() : request.goals(),
                request.allergies() == null ? fallback.allergies() : request.allergies(),
                request.preferences() == null ? fallback.preferences() : request.preferences(),
                request.supplementContext() == null ? fallback.supplementContext() : request.supplementContext(),
                status,
                Instant.now().toString()
        );
    }

    private OnboardingProfileResponse skeletonProfile(OnboardingStatus status) {
        return new OnboardingProfileResponse(
                AgeRange.AGE_30_39,
                1,
                new Money(100000, "KRW"),
                List.of(HealthGoalCode.BALANCED_NUTRITION),
                List.of(),
                new FoodPreferenceInput(List.of(), List.of(), List.of()),
                new SupplementContextInput(false, List.of(), false, false),
                status,
                Instant.now().toString()
        );
    }
}
