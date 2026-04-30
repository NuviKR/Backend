package com.nuvi.nuvi.profile.infra.entity;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.HealthGoalCode;
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
@Table(name = "health_goals")
public class HealthGoalJpaEntity {

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
    @Column(name = "goal_code", nullable = false, length = 40)
    private HealthGoalCode goalCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected HealthGoalJpaEntity() {
    }

    public HealthGoalJpaEntity(String memberId, HealthGoalCode goalCode, Instant createdAt) {
        this.memberId = memberId;
        this.goalCode = goalCode;
        this.createdAt = createdAt;
    }

    void attachTo(UserProfileJpaEntity profile) {
        this.profile = profile;
    }

    public HealthGoalCode goalCode() {
        return goalCode;
    }
}
