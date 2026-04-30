package com.nuvi.nuvi.profile.domain.repository;

import com.nuvi.nuvi.profile.domain.model.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    Optional<UserProfile> findByMemberId(String memberId);

    UserProfile save(UserProfile profile);
}
