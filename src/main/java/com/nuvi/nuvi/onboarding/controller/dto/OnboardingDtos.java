package com.nuvi.nuvi.onboarding.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class OnboardingDtos {

    private OnboardingDtos() {
    }

    public enum AgeRange {
        AGE_20_29,
        AGE_30_39,
        AGE_40_49,
        AGE_50_PLUS
    }

    public enum HealthGoalCode {
        WEIGHT_MANAGEMENT,
        BALANCED_NUTRITION,
        ENERGY_SUPPORT,
        DIGESTIVE_COMFORT,
        IMMUNE_SUPPORT,
        BUDGET_OPTIMIZATION
    }

    public enum AllergySeverity {
        MILD_PREFERENCE,
        INTOLERANCE,
        ALLERGY,
        ANAPHYLACTIC
    }

    public enum DietType {
        NONE,
        VEGAN,
        VEGETARIAN,
        PESCATARIAN,
        LOW_SUGAR,
        HIGH_PROTEIN
    }

    public enum OnboardingStatus {
        DRAFT,
        COMPLETE
    }

    public record Money(
            @Min(0)
            @Max(10_000_000)
            int amount,
            @NotBlank
            @Pattern(regexp = "KRW")
            String currency
    ) {
    }

    public record AllergyInput(
            @NotBlank
            @Size(max = 64)
            String allergenCode,
            @NotNull
            AllergySeverity severity
    ) {
    }

    public record FoodPreferenceInput(
            @Size(max = 50)
            List<String> excludedIngredientCodes,
            @Size(max = 50)
            List<String> preferredIngredientCodes,
            @Size(max = 5)
            List<DietType> dietTypes
    ) {
    }

    public record SupplementContextInput(
            boolean usesSupplements,
            @Size(max = 30)
            List<String> supplementIngredientCodes,
            boolean usesMedication,
            Boolean expertConsultationNoticeAccepted
    ) {
    }

    public record OnboardingProfileCreateRequest(
            @NotNull
            AgeRange ageRange,
            @NotNull
            @Min(1)
            @Max(8)
            Integer householdSize,
            @NotNull
            @Valid
            Money weeklyBudget,
            @NotNull
            @Size(min = 1, max = 5)
            List<HealthGoalCode> goals,
            @NotNull
            @Size(max = 30)
            List<@Valid AllergyInput> allergies,
            @NotNull
            @Valid
            FoodPreferenceInput preferences,
            @NotNull
            @Valid
            SupplementContextInput supplementContext
    ) {
    }

    public record OnboardingProfilePatchRequest(
            AgeRange ageRange,
            @Min(1)
            @Max(8)
            Integer householdSize,
            @Valid
            Money weeklyBudget,
            @Size(max = 5)
            List<HealthGoalCode> goals,
            @Size(max = 30)
            List<@Valid AllergyInput> allergies,
            @Valid
            FoodPreferenceInput preferences,
            @Valid
            SupplementContextInput supplementContext
    ) {
    }

    public record OnboardingProfileResponse(
            AgeRange ageRange,
            int householdSize,
            Money weeklyBudget,
            List<HealthGoalCode> goals,
            List<AllergyInput> allergies,
            FoodPreferenceInput preferences,
            SupplementContextInput supplementContext,
            OnboardingStatus status,
            String updatedAt
    ) {
    }

    public record OnboardingCompletionResponse(
            boolean onboardingCompleted,
            String nextAction
    ) {
    }
}
