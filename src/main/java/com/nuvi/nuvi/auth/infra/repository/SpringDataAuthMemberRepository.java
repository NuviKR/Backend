package com.nuvi.nuvi.auth.infra.repository;

import com.nuvi.nuvi.auth.infra.entity.AuthMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuthMemberRepository extends JpaRepository<AuthMemberJpaEntity, String> {
}
