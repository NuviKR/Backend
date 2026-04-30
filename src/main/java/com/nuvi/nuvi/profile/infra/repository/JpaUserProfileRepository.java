package com.nuvi.nuvi.profile.infra.repository;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.DietType;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.FoodPreferenceType;
import com.nuvi.nuvi.profile.domain.model.UserProfile;
import com.nuvi.nuvi.profile.domain.repository.UserProfileRepository;
import com.nuvi.nuvi.profile.infra.entity.AllergyJpaEntity;
import com.nuvi.nuvi.profile.infra.entity.FoodPreferenceJpaEntity;
import com.nuvi.nuvi.profile.infra.entity.HealthGoalJpaEntity;
import com.nuvi.nuvi.profile.infra.entity.UserProfileJpaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
class JpaUserProfileRepository implements UserProfileRepository {

    private final SpringDataUserProfileRepository repository;

    JpaUserProfileRepository(SpringDataUserProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByMemberId(String memberId) {
        return repository.findByMemberId(memberId).map(this::toDomain);
    }

    @Override
    @Transactional
    public UserProfile save(UserProfile profile) {
        Instant now = Instant.now();
        UserProfileJpaEntity entity = repository.findByMemberId(profile.memberId())
                .orElseGet(() -> UserProfileJpaEntity.create(profile.memberId(), now));

        entity.replaceCore(
                profile.ageRange(),
                profile.householdSize(),
                profile.weeklyBudgetAmount(),
                profile.weeklyBudgetCurrency(),
                profile.supplementContext().usesSupplements(),
                String.join(",", profile.supplementContext().supplementIngredientCodes()),
                profile.supplementContext().usesMedication(),
                profile.supplementContext().expertConsultationNoticeAccepted(),
                profile.status(),
                now
        );
        entity.replaceAllergies(profile.allergies().stream()
                .map(allergy -> new AllergyJpaEntity(profile.memberId(), allergy.allergenCode(), allergy.severity(), now))
                .toList());
        entity.replaceHealthGoals(profile.goals().stream()
                .map(goal -> new HealthGoalJpaEntity(profile.memberId(), goal, now))
                .toList());
        entity.replaceFoodPreferences(foodPreferences(profile, now));
        return toDomain(repository.save(entity));
    }

    private List<FoodPreferenceJpaEntity> foodPreferences(UserProfile profile, Instant now) {
        UserProfile.FoodPreference preferences = profile.preferences();
        List<FoodPreferenceJpaEntity> excluded = preferences.excludedIngredientCodes().stream()
                .map(code -> new FoodPreferenceJpaEntity(profile.memberId(), FoodPreferenceType.EXCLUDED_INGREDIENT, code, now))
                .toList();
        List<FoodPreferenceJpaEntity> preferred = preferences.preferredIngredientCodes().stream()
                .map(code -> new FoodPreferenceJpaEntity(profile.memberId(), FoodPreferenceType.PREFERRED_INGREDIENT, code, now))
                .toList();
        List<FoodPreferenceJpaEntity> dietTypes = preferences.dietTypes().stream()
                .map(type -> new FoodPreferenceJpaEntity(profile.memberId(), FoodPreferenceType.DIET_TYPE, type.name(), now))
                .toList();
        return java.util.stream.Stream.of(excluded, preferred, dietTypes)
                .flatMap(List::stream)
                .toList();
    }

    private UserProfile toDomain(UserProfileJpaEntity entity) {
        List<String> excluded = preferencesOfType(entity, FoodPreferenceType.EXCLUDED_INGREDIENT);
        List<String> preferred = preferencesOfType(entity, FoodPreferenceType.PREFERRED_INGREDIENT);
        List<DietType> dietTypes = preferencesOfType(entity, FoodPreferenceType.DIET_TYPE).stream()
                .map(DietType::valueOf)
                .toList();
        return new UserProfile(
                entity.memberId(),
                entity.ageRange(),
                entity.householdSize(),
                entity.weeklyBudgetAmount(),
                entity.weeklyBudgetCurrency(),
                entity.healthGoals().stream()
                        .map(HealthGoalJpaEntity::goalCode)
                        .sorted(Comparator.comparing(Enum::name))
                        .toList(),
                entity.allergies().stream()
                        .map(allergy -> new UserProfile.Allergy(allergy.allergenCode(), allergy.severity()))
                        .sorted(Comparator.comparing(UserProfile.Allergy::allergenCode))
                        .toList(),
                new UserProfile.FoodPreference(excluded, preferred, dietTypes),
                new UserProfile.SupplementContext(
                        entity.usesSupplements(),
                        splitCodes(entity.supplementIngredientCodes()),
                        entity.usesMedication(),
                        entity.expertConsultationNoticeAccepted()
                ),
                entity.status(),
                entity.updatedAt()
        );
    }

    private static List<String> preferencesOfType(UserProfileJpaEntity entity, FoodPreferenceType type) {
        return entity.foodPreferences().stream()
                .filter(preference -> preference.preferenceType() == type)
                .map(FoodPreferenceJpaEntity::preferenceCode)
                .sorted()
                .toList();
    }

    private static List<String> splitCodes(String rawCodes) {
        if (rawCodes == null || rawCodes.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(rawCodes.split(","))
                .filter(code -> !code.isBlank())
                .sorted()
                .toList();
    }
}
