package com.nuvi.nuvi.auth.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_members")
public class AuthMemberJpaEntity {

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

    public static AuthMemberJpaEntity create() {
        String id = "mem_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthMemberJpaEntity(id, Instant.now());
    }

    public String memberId() {
        return memberId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
