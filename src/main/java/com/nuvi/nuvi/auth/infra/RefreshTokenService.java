package com.nuvi.nuvi.auth.infra;

import com.nuvi.nuvi.common.api.ApiErrorCode;
import com.nuvi.nuvi.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SpringDataAuthRefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties properties;

    public RefreshTokenService(
            SpringDataAuthRefreshTokenRepository refreshTokenRepository,
            RefreshTokenProperties properties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
    }

    @Transactional
    public IssuedRefreshToken issue(String memberId) {
        Instant now = Instant.now();
        String token = generateToken();
        refreshTokenRepository.save(AuthRefreshTokenJpaEntity.issue(
                memberId,
                hashToken(token),
                now,
                now.plus(properties.ttl())
        ));
        return new IssuedRefreshToken(token, memberId);
    }

    @Transactional
    public IssuedRefreshToken rotate(String refreshToken) {
        AuthRefreshTokenJpaEntity current = findUsable(refreshToken);
        String memberId = current.memberId();
        current.revoke(Instant.now());
        return issue(memberId);
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .ifPresent(token -> token.revoke(Instant.now()));
    }

    private AuthRefreshTokenJpaEntity findUsable(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw authRequired();
        }
        Instant now = Instant.now();
        return refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .filter(token -> token.isUsableAt(now))
                .orElseThrow(RefreshTokenService::authRequired);
    }

    private static ApiException authRequired() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.AUTH_REQUIRED,
                ApiErrorCode.AUTH_REQUIRED.defaultMessage()
        );
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    public record IssuedRefreshToken(String token, String memberId) {
    }
}
