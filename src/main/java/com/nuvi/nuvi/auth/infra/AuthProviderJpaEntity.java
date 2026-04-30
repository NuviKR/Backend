package com.nuvi.nuvi.auth.infra;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;
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
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "auth_providers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_auth_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
class AuthProviderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private AuthMemberJpaEntity member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, updatable = false, length = 30)
    private AuthProvider provider;

    @Column(name = "provider_subject", nullable = false, updatable = false, length = 255)
    private String providerSubject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuthProviderJpaEntity() {
    }

    private AuthProviderJpaEntity(
            AuthMemberJpaEntity member,
            AuthProvider provider,
            String providerSubject,
            Instant createdAt
    ) {
        this.member = member;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.createdAt = createdAt;
    }

    static AuthProviderJpaEntity create(AuthMemberJpaEntity member, AuthProvider provider, String providerSubject) {
        return new AuthProviderJpaEntity(member, provider, providerSubject, Instant.now());
    }

    Long id() {
        return id;
    }

    AuthMemberJpaEntity member() {
        return member;
    }

    AuthProvider provider() {
        return provider;
    }

    String providerSubject() {
        return providerSubject;
    }

    Instant createdAt() {
        return createdAt;
    }
}
