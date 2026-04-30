package com.nuvi.nuvi.profile.domain.model;

public final class ProfileEnums {

    private ProfileEnums() {
    }

    public enum AgeRange {
        AGE_20_29,
        AGE_30_39,
        AGE_40_49,
        AGE_50_PLUS
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

    public enum FoodPreferenceType {
        EXCLUDED_INGREDIENT,
        PREFERRED_INGREDIENT,
        DIET_TYPE
    }

    public enum HealthGoalCode {
        WEIGHT_MANAGEMENT,
        BALANCED_NUTRITION,
        ENERGY_SUPPORT,
        DIGESTIVE_COMFORT,
        IMMUNE_SUPPORT,
        BUDGET_OPTIMIZATION
    }

    public enum ProfileStatus {
        DRAFT,
        COMPLETE
    }
}
