package com.nuvi.nuvi.auth.infra.repository;

import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.infra.entity.AuthProviderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataAuthProviderRepository extends JpaRepository<AuthProviderJpaEntity, Long> {

    Optional<AuthProviderJpaEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
