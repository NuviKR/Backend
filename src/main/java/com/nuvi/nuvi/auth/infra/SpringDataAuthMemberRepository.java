package com.nuvi.nuvi.auth.infra;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuthMemberRepository extends JpaRepository<AuthMemberJpaEntity, String> {
}
