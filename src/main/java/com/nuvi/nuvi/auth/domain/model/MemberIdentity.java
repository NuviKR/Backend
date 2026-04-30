package com.nuvi.nuvi.auth.domain.model;


public record MemberIdentity(
        String memberId,
        AuthProvider provider,
        String providerSubject
) {
}
