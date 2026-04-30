package com.nuvi.nuvi.profile.infra.entity;

import com.nuvi.nuvi.profile.domain.model.ProfileEnums.AllergySeverity;
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
@Table(name = "allergies")
public class AllergyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfileJpaEntity profile;

    @Column(name = "member_id", nullable = false, length = 40)
    private String memberId;

    @Column(name = "allergen_code", nullable = false, length = 64)
    private String allergenCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private AllergySeverity severity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AllergyJpaEntity() {
    }

    public AllergyJpaEntity(String memberId, String allergenCode, AllergySeverity severity, Instant now) {
        this.memberId = memberId;
        this.allergenCode = allergenCode;
        this.severity = severity;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void attachTo(UserProfileJpaEntity profile) {
        this.profile = profile;
    }

    public String allergenCode() {
        return allergenCode;
    }

    public AllergySeverity severity() {
        return severity;
    }
}
