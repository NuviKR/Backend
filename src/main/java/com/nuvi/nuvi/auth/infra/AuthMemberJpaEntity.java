package com.nuvi.nuvi.auth.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_members")
class AuthMemberJpaEntity {

    @Id
    @Column(name = "member_id", nullable = false, updatable = false, length = 40)
    private String memberId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuthMemberJpaEntity() {
    }

    private AuthMemberJpaEntity(String memberId, Instant createdAt) {
        this.memberId = memberId;
        this.createdAt = createdAt;
    }

    static AuthMemberJpaEntity create() {
        String id = "mem_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthMemberJpaEntity(id, Instant.now());
    }

    String memberId() {
        return memberId;
    }

    Instant createdAt() {
        return createdAt;
    }
}
