package com.nuvi.nuvi.auth.infra;

import com.nuvi.nuvi.auth.controller.dto.AuthDtos.AuthProvider;
import com.nuvi.nuvi.auth.domain.MemberIdentity;
import com.nuvi.nuvi.auth.domain.OidcMemberRepository;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOidcMemberRepository implements OidcMemberRepository {

    private final Map<String, MemberIdentity> members = new ConcurrentHashMap<>();

    @Override
    public MemberIdentity findOrCreate(AuthProvider provider, String providerSubject) {
        String key = provider.name() + ":" + providerSubject;
        return members.computeIfAbsent(key, ignored ->
                new MemberIdentity(memberIdFor(provider, providerSubject), provider, providerSubject)
        );
    }

    private String memberIdFor(AuthProvider provider, String providerSubject) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((provider.name() + ":" + providerSubject).getBytes(StandardCharsets.UTF_8));
            return "mem_" + HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
