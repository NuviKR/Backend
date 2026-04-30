package com.nuvi.nuvi.profile.infra.repository;

import com.nuvi.nuvi.profile.infra.entity.UserProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserProfileRepository extends JpaRepository<UserProfileJpaEntity, Long> {

    Optional<UserProfileJpaEntity> findByMemberId(String memberId);
}
