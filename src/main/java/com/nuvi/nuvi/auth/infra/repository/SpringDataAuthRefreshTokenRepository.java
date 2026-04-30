package com.nuvi.nuvi.auth.infra.repository;

import com.nuvi.nuvi.auth.infra.entity.AuthRefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataAuthRefreshTokenRepository extends JpaRepository<AuthRefreshTokenJpaEntity, Long> {

    Optional<AuthRefreshTokenJpaEntity> findByTokenHash(String tokenHash);
}
