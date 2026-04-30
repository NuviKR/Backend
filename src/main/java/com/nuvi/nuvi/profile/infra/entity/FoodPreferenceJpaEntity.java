package com.nuvi.nuvi.profile.infra.entity;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.FoodPreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "food_preferences")
public class FoodPreferenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfileJpaEntity profile;

    @Column(name = "member_id", nullable = false, length = 40)
    private String memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_type", nullable = false, length = 20)
    private FoodPreferenceType preferenceType;

    @Column(name = "preference_code", nullable = false, length = 64)
    private String preferenceCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FoodPreferenceJpaEntity() {
    }

    public FoodPreferenceJpaEntity(String memberId, FoodPreferenceType preferenceType, String preferenceCode, Instant createdAt) {
        this.memberId = memberId;
        this.preferenceType = preferenceType;
        this.preferenceCode = preferenceCode;
        this.createdAt = createdAt;
    }

    void attachTo(UserProfileJpaEntity profile) {
        this.profile = profile;
    }

    public FoodPreferenceType preferenceType() {
        return preferenceType;
    }

    public String preferenceCode() {
        return preferenceCode;
    }
}
