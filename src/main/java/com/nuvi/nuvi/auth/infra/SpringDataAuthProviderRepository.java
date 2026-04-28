package com.nuvi.nuvi.auth.infra;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataAuthProviderRepository extends JpaRepository<AuthProviderJpaEntity, Long> {

    Optional<AuthProviderJpaEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
