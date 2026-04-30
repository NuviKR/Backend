package com.nuvi.nuvi.profile.domain.model;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.AgeRange;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.AllergySeverity;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.DietType;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.HealthGoalCode;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.ProfileStatus;

import java.time.Instant;
import java.util.List;

public record UserProfile(
        String memberId,
        AgeRange ageRange,
        int householdSize,
        int weeklyBudgetAmount,
        String weeklyBudgetCurrency,
        List<HealthGoalCode> goals,
        List<Allergy> allergies,
        FoodPreference preferences,
        SupplementContext supplementContext,
        ProfileStatus status,
        Instant updatedAt
) {

    public UserProfile {
        goals = List.copyOf(goals == null ? List.of() : goals);
        allergies = List.copyOf(allergies == null ? List.of() : allergies);
    }

    public record Allergy(String allergenCode, AllergySeverity severity) {
    }

    public record FoodPreference(
            List<String> excludedIngredientCodes,
            List<String> preferredIngredientCodes,
            List<DietType> dietTypes
    ) {
        public FoodPreference {
            excludedIngredientCodes = List.copyOf(excludedIngredientCodes == null ? List.of() : excludedIngredientCodes);
            preferredIngredientCodes = List.copyOf(preferredIngredientCodes == null ? List.of() : preferredIngredientCodes);
            dietTypes = List.copyOf(dietTypes == null ? List.of() : dietTypes);
        }
    }

    public record SupplementContext(
            boolean usesSupplements,
            List<String> supplementIngredientCodes,
            boolean usesMedication,
            boolean expertConsultationNoticeAccepted
    ) {
        public SupplementContext {
            supplementIngredientCodes = List.copyOf(supplementIngredientCodes == null ? List.of() : supplementIngredientCodes);
        }
    }
}
