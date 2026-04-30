package com.nuvi.nuvi.auth.infra.repository;

import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.domain.model.MemberIdentity;
import com.nuvi.nuvi.auth.domain.repository.OidcMemberRepository;
import com.nuvi.nuvi.auth.infra.entity.AuthMemberJpaEntity;
import com.nuvi.nuvi.auth.infra.entity.AuthProviderJpaEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaOidcMemberRepository implements OidcMemberRepository {

    private final SpringDataAuthMemberRepository memberRepository;
    private final SpringDataAuthProviderRepository authProviderRepository;

    public JpaOidcMemberRepository(
            SpringDataAuthMemberRepository memberRepository,
            SpringDataAuthProviderRepository authProviderRepository
    ) {
        this.memberRepository = memberRepository;
        this.authProviderRepository = authProviderRepository;
    }

    @Override
    @Transactional
    public MemberIdentity findOrCreate(AuthProvider provider, String providerSubject) {
        return authProviderRepository.findByProviderAndProviderSubject(provider, providerSubject)
                .map(this::toIdentity)
                .orElseGet(() -> create(provider, providerSubject));
    }

    private MemberIdentity create(AuthProvider provider, String providerSubject) {
        try {
            AuthMemberJpaEntity member = memberRepository.save(AuthMemberJpaEntity.create());
            AuthProviderJpaEntity authProvider = authProviderRepository.save(
                    AuthProviderJpaEntity.create(member, provider, providerSubject)
            );
            return toIdentity(authProvider);
        } catch (DataIntegrityViolationException exception) {
            return authProviderRepository.findByProviderAndProviderSubject(provider, providerSubject)
                    .map(this::toIdentity)
                    .orElseThrow(() -> exception);
        }
    }

    private MemberIdentity toIdentity(AuthProviderJpaEntity authProvider) {
        return new MemberIdentity(
                authProvider.member().memberId(),
                authProvider.provider(),
                authProvider.providerSubject()
        );
    }
}
