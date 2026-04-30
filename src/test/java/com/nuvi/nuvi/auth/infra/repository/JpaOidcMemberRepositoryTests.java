package com.nuvi.nuvi.auth.infra.repository;

import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.domain.repository.OidcMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaOidcMemberRepositoryTests {

    @Autowired
    private OidcMemberRepository oidcMemberRepository;

    @Autowired
    private SpringDataAuthMemberRepository memberRepository;

    @Autowired
    private SpringDataAuthProviderRepository authProviderRepository;

    @Test
    void sameOidcSubjectMapsToSameMember() {
        var first = oidcMemberRepository.findOrCreate(AuthProvider.KAKAO, "kakao_subject_same");
        var second = oidcMemberRepository.findOrCreate(AuthProvider.KAKAO, "kakao_subject_same");

        assertThat(second.memberId()).isEqualTo(first.memberId());
        assertThat(authProviderRepository.findByProviderAndProviderSubject(AuthProvider.KAKAO, "kakao_subject_same"))
                .isPresent()
                .get()
                .extracting(provider -> provider.member().memberId())
                .isEqualTo(first.memberId());
    }

    @Test
    void newOidcSubjectCreatesMemberAndAuthProviderRows() {
        long memberCountBefore = memberRepository.count();
        long authProviderCountBefore = authProviderRepository.count();

        var created = oidcMemberRepository.findOrCreate(AuthProvider.KAKAO, "kakao_subject_new");

        assertThat(created.memberId()).startsWith("mem_");
        assertThat(memberRepository.count()).isEqualTo(memberCountBefore + 1);
        assertThat(authProviderRepository.count()).isEqualTo(authProviderCountBefore + 1);
        assertThat(authProviderRepository.findByProviderAndProviderSubject(AuthProvider.KAKAO, "kakao_subject_new"))
                .isPresent()
                .get()
                .satisfies(provider -> {
                    assertThat(provider.member().memberId()).isEqualTo(created.memberId());
                    assertThat(provider.provider()).isEqualTo(AuthProvider.KAKAO);
                    assertThat(provider.providerSubject()).isEqualTo("kakao_subject_new");
                });
    }
}
