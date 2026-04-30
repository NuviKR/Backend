package com.nuvi.nuvi.profile.infra.entity;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.AgeRange;
import com.nuvi.nuvi.profile.domain.model.ProfileEnums.ProfileStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_profiles_member_id", columnNames = "member_id")
)
public class UserProfileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "member_id", nullable = false, updatable = false, length = 40)
    private String memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false, length = 20)
    private AgeRange ageRange;

    @Column(name = "household_size", nullable = false)
    private int householdSize;

    @Column(name = "weekly_budget_amount", nullable = false)
    private int weeklyBudgetAmount;

    @Column(name = "weekly_budget_currency", nullable = false, length = 3)
    private String weeklyBudgetCurrency;

    @Column(name = "uses_supplements", nullable = false)
    private boolean usesSupplements;

    @Column(name = "supplement_ingredient_codes", nullable = false, length = 2048)
    private String supplementIngredientCodes;

    @Column(name = "uses_medication", nullable = false)
    private boolean usesMedication;

    @Column(name = "expert_consultation_notice_accepted", nullable = false)
    private boolean expertConsultationNoticeAccepted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProfileStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllergyJpaEntity> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodPreferenceJpaEntity> foodPreferences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthGoalJpaEntity> healthGoals = new ArrayList<>();

    protected UserProfileJpaEntity() {
    }

    public static UserProfileJpaEntity create(String memberId, Instant now) {
        UserProfileJpaEntity profile = new UserProfileJpaEntity();
        profile.memberId = memberId;
        profile.createdAt = now;
        profile.updatedAt = now;
        return profile;
    }

    public Long id() {
        return id;
    }

    public String memberId() {
        return memberId;
    }

    public AgeRange ageRange() {
        return ageRange;
    }

    public int householdSize() {
        return householdSize;
    }

    public int weeklyBudgetAmount() {
        return weeklyBudgetAmount;
    }

    public String weeklyBudgetCurrency() {
        return weeklyBudgetCurrency;
    }

    public boolean usesSupplements() {
        return usesSupplements;
    }

    public String supplementIngredientCodes() {
        return supplementIngredientCodes;
    }

    public boolean usesMedication() {
        return usesMedication;
    }

    public boolean expertConsultationNoticeAccepted() {
        return expertConsultationNoticeAccepted;
    }

    public ProfileStatus status() {
        return status;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<AllergyJpaEntity> allergies() {
        return List.copyOf(allergies);
    }

    public List<FoodPreferenceJpaEntity> foodPreferences() {
        return List.copyOf(foodPreferences);
    }

    public List<HealthGoalJpaEntity> healthGoals() {
        return List.copyOf(healthGoals);
    }

    public void replaceCore(
            AgeRange ageRange,
            int householdSize,
            int weeklyBudgetAmount,
            String weeklyBudgetCurrency,
            boolean usesSupplements,
            String supplementIngredientCodes,
            boolean usesMedication,
            boolean expertConsultationNoticeAccepted,
            ProfileStatus status,
            Instant now
    ) {
        this.ageRange = ageRange;
        this.householdSize = householdSize;
        this.weeklyBudgetAmount = weeklyBudgetAmount;
        this.weeklyBudgetCurrency = weeklyBudgetCurrency;
        this.usesSupplements = usesSupplements;
        this.supplementIngredientCodes = supplementIngredientCodes;
        this.usesMedication = usesMedication;
        this.expertConsultationNoticeAccepted = expertConsultationNoticeAccepted;
        this.status = status;
        this.updatedAt = now;
        if (status == ProfileStatus.COMPLETE && completedAt == null) {
            completedAt = now;
        }
    }

    public void replaceAllergies(List<AllergyJpaEntity> replacement) {
        allergies.clear();
        replacement.forEach(allergy -> {
            allergy.attachTo(this);
            allergies.add(allergy);
        });
    }

    public void replaceFoodPreferences(List<FoodPreferenceJpaEntity> replacement) {
        foodPreferences.clear();
        replacement.forEach(preference -> {
            preference.attachTo(this);
            foodPreferences.add(preference);
        });
    }

    public void replaceHealthGoals(List<HealthGoalJpaEntity> replacement) {
        healthGoals.clear();
        replacement.forEach(goal -> {
            goal.attachTo(this);
            healthGoals.add(goal);
        });
    }
}
