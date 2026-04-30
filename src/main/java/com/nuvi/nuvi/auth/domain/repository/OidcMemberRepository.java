package com.nuvi.nuvi.auth.domain.repository;

import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.domain.model.MemberIdentity;

public interface OidcMemberRepository {

    MemberIdentity findOrCreate(AuthProvider provider, String providerSubject);
}
