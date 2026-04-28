package com.nuvi.nuvi.onboarding.controller.dto;

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
            int amount,
            String currency
    ) {
    }

    public record AllergyInput(
            String allergenCode,
            AllergySeverity severity
    ) {
    }

    public record FoodPreferenceInput(
            List<String> excludedIngredientCodes,
            List<String> preferredIngredientCodes,
            List<DietType> dietTypes
    ) {
    }

    public record SupplementContextInput(
            boolean usesSupplements,
            List<String> supplementIngredientCodes,
            boolean usesMedication,
            Boolean expertConsultationNoticeAccepted
    ) {
    }

    public record OnboardingProfileRequest(
            AgeRange ageRange,
            Integer householdSize,
            Money weeklyBudget,
            List<HealthGoalCode> goals,
            List<AllergyInput> allergies,
            FoodPreferenceInput preferences,
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
