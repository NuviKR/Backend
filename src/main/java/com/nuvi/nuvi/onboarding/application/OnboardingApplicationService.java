package com.nuvi.nuvi.onboarding.application;

import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.AgeRange;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.AllergyInput;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.FoodPreferenceInput;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.HealthGoalCode;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.Money;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingCompletionResponse;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileCreateRequest;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfilePatchRequest;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingProfileResponse;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.OnboardingStatus;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.SupplementContextInput;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.ProfileStatus;
import com.nuvi.nuvi.profile.domain.model.UserProfile;
import com.nuvi.nuvi.profile.domain.repository.UserProfileRepository;
import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OnboardingApplicationService {

    private final UserProfileRepository profileRepository;

    public OnboardingApplicationService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public OnboardingProfileResponse getProfile(String memberId) {
        return profileRepository.findByMemberId(memberId)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, ApiErrorCode.PROFILE_INCOMPLETE));
    }

    @Transactional
    public OnboardingProfileResponse createProfile(String memberId, OnboardingProfileCreateRequest request) {
        UserProfile profile = new UserProfile(
                memberId,
                map(request.ageRange()),
                request.householdSize(),
                request.weeklyBudget().amount(),
                request.weeklyBudget().currency(),
                request.goals().stream().map(this::map).toList(),
                request.allergies().stream().map(this::map).toList(),
                map(request.preferences()),
                map(request.supplementContext()),
                ProfileStatus.DRAFT,
                null
        );
        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public OnboardingProfileResponse patchProfile(String memberId, OnboardingProfilePatchRequest request) {
        UserProfile existing = profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, ApiErrorCode.PROFILE_INCOMPLETE));

        UserProfile patched = new UserProfile(
                memberId,
                request.ageRange() == null ? existing.ageRange() : map(request.ageRange()),
                request.householdSize() == null ? existing.householdSize() : request.householdSize(),
                request.weeklyBudget() == null ? existing.weeklyBudgetAmount() : request.weeklyBudget().amount(),
                request.weeklyBudget() == null ? existing.weeklyBudgetCurrency() : request.weeklyBudget().currency(),
                request.goals() == null ? existing.goals() : request.goals().stream().map(this::map).toList(),
                request.allergies() == null ? existing.allergies() : request.allergies().stream().map(this::map).toList(),
                request.preferences() == null ? existing.preferences() : map(request.preferences()),
                request.supplementContext() == null ? existing.supplementContext() : map(request.supplementContext()),
                existing.status(),
                existing.updatedAt()
        );
        return toResponse(profileRepository.save(patched));
    }

    @Transactional
    public OnboardingCompletionResponse completeOnboarding(String memberId) {
        UserProfile existing = profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, ApiErrorCode.PROFILE_INCOMPLETE));
        UserProfile completed = new UserProfile(
                existing.memberId(),
                existing.ageRange(),
                existing.householdSize(),
                existing.weeklyBudgetAmount(),
                existing.weeklyBudgetCurrency(),
                existing.goals(),
                existing.allergies(),
                existing.preferences(),
                existing.supplementContext(),
                ProfileStatus.COMPLETE,
                existing.updatedAt()
        );
        profileRepository.save(completed);
        return new OnboardingCompletionResponse(true, "GENERATE_WEEKLY_CART");
    }

    private OnboardingProfileResponse toResponse(UserProfile profile) {
        return new OnboardingProfileResponse(
                AgeRange.valueOf(profile.ageRange().name()),
                profile.householdSize(),
                new Money(profile.weeklyBudgetAmount(), profile.weeklyBudgetCurrency()),
                profile.goals().stream().map(goal -> HealthGoalCode.valueOf(goal.name())).toList(),
                profile.allergies().stream()
                        .map(allergy -> new AllergyInput(
                                allergy.allergenCode(),
                                com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.AllergySeverity.valueOf(allergy.severity().name())
                        ))
                        .toList(),
                new FoodPreferenceInput(
                        profile.preferences().excludedIngredientCodes(),
                        profile.preferences().preferredIngredientCodes(),
                        profile.preferences().dietTypes().stream()
                                .map(type -> com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.DietType.valueOf(type.name()))
                                .toList()
                ),
                new SupplementContextInput(
                        profile.supplementContext().usesSupplements(),
                        profile.supplementContext().supplementIngredientCodes(),
                        profile.supplementContext().usesMedication(),
                        profile.supplementContext().expertConsultationNoticeAccepted()
                ),
                OnboardingStatus.valueOf(profile.status().name()),
                profile.updatedAt().toString()
        );
    }

    private com.nuvi.nuvi.profile.domain.model.ProfileEnums.AgeRange map(AgeRange ageRange) {
        return com.nuvi.nuvi.profile.domain.model.ProfileEnums.AgeRange.valueOf(ageRange.name());
    }

    private com.nuvi.nuvi.profile.domain.model.ProfileEnums.HealthGoalCode map(HealthGoalCode goal) {
        return com.nuvi.nuvi.profile.domain.model.ProfileEnums.HealthGoalCode.valueOf(goal.name());
    }

    private UserProfile.Allergy map(AllergyInput allergy) {
        return new UserProfile.Allergy(
                allergy.allergenCode(),
                com.nuvi.nuvi.profile.domain.model.ProfileEnums.AllergySeverity.valueOf(allergy.severity().name())
        );
    }

    private UserProfile.FoodPreference map(FoodPreferenceInput preferences) {
        return new UserProfile.FoodPreference(
                preferences.excludedIngredientCodes() == null ? List.of() : preferences.excludedIngredientCodes(),
                preferences.preferredIngredientCodes() == null ? List.of() : preferences.preferredIngredientCodes(),
                preferences.dietTypes() == null ? List.of() : preferences.dietTypes().stream()
                        .map(type -> com.nuvi.nuvi.profile.domain.model.ProfileEnums.DietType.valueOf(type.name()))
                        .toList()
        );
    }

    private UserProfile.SupplementContext map(SupplementContextInput context) {
        return new UserProfile.SupplementContext(
                context.usesSupplements(),
                context.supplementIngredientCodes() == null ? List.of() : context.supplementIngredientCodes(),
                context.usesMedication(),
                Boolean.TRUE.equals(context.expertConsultationNoticeAccepted())
        );
    }
}
