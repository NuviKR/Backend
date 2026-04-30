package com.nuvi.nuvi.auth.domain;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;

public record MemberIdentity(
        String memberId,
        AuthProvider provider,
        String providerSubject
) {
}
