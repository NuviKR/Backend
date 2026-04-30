package com.nuvi.nuvi.auth.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "auth_refresh_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_auth_refresh_token_hash",
                columnNames = "token_hash"
        )
)
public class AuthRefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "member_id", nullable = false, updatable = false, length = 40)
    private String memberId;

    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected AuthRefreshTokenJpaEntity() {
    }

    private AuthRefreshTokenJpaEntity(String memberId, String tokenHash, Instant createdAt, Instant expiresAt) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static AuthRefreshTokenJpaEntity issue(String memberId, String tokenHash, Instant createdAt, Instant expiresAt) {
        return new AuthRefreshTokenJpaEntity(memberId, tokenHash, createdAt, expiresAt);
    }

    public String memberId() {
        return memberId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public boolean isUsableAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke(Instant revokedAt) {
        if (this.revokedAt == null) {
            this.revokedAt = revokedAt;
        }
    }
}
