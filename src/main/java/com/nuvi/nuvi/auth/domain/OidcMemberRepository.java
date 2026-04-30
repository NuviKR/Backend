package com.nuvi.nuvi.auth.domain;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;

public interface OidcMemberRepository {

    MemberIdentity findOrCreate(AuthProvider provider, String providerSubject);
}
