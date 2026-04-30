package com.nuvi.nuvi.auth.infra.adapter;

import com.nuvi.nuvi.auth.infra.config.KakaoAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

@Component
class KakaoOidcRestClient implements KakaoOidcClient {

    private static final String DEFAULT_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String DEFAULT_ISSUER = "https://kauth.kakao.com";
    private static final String DEFAULT_JWKS_URI = "https://kauth.kakao.com/.well-known/jwks.json";

    private final KakaoAuthProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public KakaoOidcRestClient(KakaoAuthProperties properties, ObjectMapper objectMapper) {
        this(properties, RestClient.create(), objectMapper, Clock.systemUTC());
    }

    KakaoOidcRestClient(
            KakaoAuthProperties properties,
            RestClient restClient,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.properties = properties;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public KakaoOidcClaims exchangeCodeAndVerifyIdToken(String code, String redirectUri) {
        try {
            KakaoTokenResponse tokenResponse = exchangeCode(code, redirectUri);
            if (tokenResponse == null || tokenResponse.idToken() == null || tokenResponse.idToken().isBlank()) {
                throw new KakaoOidcClientException("Kakao token response did not include an id_token.");
            }
            return verifyIdToken(tokenResponse.idToken());
        } catch (KakaoOidcClientException exception) {
            throw exception;
        }
    }

    private KakaoTokenResponse exchangeCode(String code, String redirectUri) {
        String clientId = properties.restApiKey();
        if (clientId == null || clientId.isBlank()) {
            throw new KakaoOidcClientException("Kakao REST API key is not configured.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
            form.add("client_secret", properties.clientSecret());
        }

        try {
            return restClient.post()
                    .uri(tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientException exception) {
            throw new KakaoOidcClientException("Kakao token exchange failed.", exception);
        }
    }

    private KakaoOidcClaims verifyIdToken(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length != 3) {
            throw new KakaoOidcClientException("Malformed id_token.");
        }

        try {
            JsonNode header = objectMapper.readTree(base64UrlDecode(parts[0]));
            JsonNode payload = objectMapper.readTree(base64UrlDecode(parts[1]));
            verifySignature(header, parts[0] + "." + parts[1], parts[2]);
            validateClaims(payload);
            return new KakaoOidcClaims(
                    requiredText(payload, "sub"),
                    requiredText(payload, "iss"),
                    audience(payload),
                    Instant.ofEpochSecond(requiredLong(payload, "exp"))
            );
        } catch (KakaoOidcClientException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new KakaoOidcClientException("id_token verification failed.", exception);
        }
    }

    private void verifySignature(JsonNode header, String signingInput, String signaturePart) {
        String alg = requiredText(header, "alg");
        if (!"RS256".equals(alg)) {
            throw new KakaoOidcClientException("Unsupported id_token algorithm.");
        }

        String kid = requiredText(header, "kid");
        RSAPublicKey publicKey = loadPublicKey(kid);

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            if (!signature.verify(Base64.getUrlDecoder().decode(signaturePart))) {
                throw new KakaoOidcClientException("Invalid id_token signature.");
            }
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new KakaoOidcClientException("id_token signature verification failed.", exception);
        }
    }

    private RSAPublicKey loadPublicKey(String kid) {
        try {
            JsonNode jwks = restClient.get()
                    .uri(jwksUri())
                    .retrieve()
                    .body(JsonNode.class);
            if (jwks == null || !jwks.has("keys")) {
                throw new KakaoOidcClientException("Kakao JWKS is empty.");
            }
            for (JsonNode key : jwks.get("keys")) {
                if (kid.equals(key.path("kid").asText())) {
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(
                            unsignedBigInteger(requiredText(key, "n")),
                            unsignedBigInteger(requiredText(key, "e"))
                    ));
                    return (RSAPublicKey) publicKey;
                }
            }
            throw new KakaoOidcClientException("Matching Kakao JWK was not found.");
        } catch (RestClientException | GeneralSecurityException exception) {
            throw new KakaoOidcClientException("Kakao JWKS lookup failed.", exception);
        }
    }

    private void validateClaims(JsonNode payload) {
        String issuer = requiredText(payload, "iss");
        if (!issuer().equals(issuer)) {
            throw new KakaoOidcClientException("Invalid id_token issuer.");
        }

        List<String> audiences = audiences(payload);
        String clientId = properties.restApiKey();
        if (clientId == null || clientId.isBlank() || !audiences.contains(clientId)) {
            throw new KakaoOidcClientException("Invalid id_token audience.");
        }

        Instant expiresAt = Instant.ofEpochSecond(requiredLong(payload, "exp"));
        if (!expiresAt.isAfter(clock.instant())) {
            throw new KakaoOidcClientException("Expired id_token.");
        }

        requiredText(payload, "sub");
    }

    private List<String> audiences(JsonNode payload) {
        JsonNode aud = payload.get("aud");
        if (aud == null) {
            throw new KakaoOidcClientException("Missing id_token audience.");
        }
        if (aud.isArray()) {
            List<String> values = new ArrayList<>();
            aud.forEach(value -> values.add(value.asText()));
            return values;
        }
        return List.of(aud.asText());
    }

    private String audience(JsonNode payload) {
        return audiences(payload).getFirst();
    }

    private String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new KakaoOidcClientException("Missing id_token field.");
        }
        return value.asText();
    }

    private long requiredLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.canConvertToLong()) {
            throw new KakaoOidcClientException("Missing id_token numeric field.");
        }
        return value.asLong();
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private BigInteger unsignedBigInteger(String base64Url) {
        return new BigInteger(1, Base64.getUrlDecoder().decode(base64Url));
    }

    private URI tokenUri() {
        return URI.create(properties.tokenUri() == null || properties.tokenUri().isBlank() ? DEFAULT_TOKEN_URI : properties.tokenUri());
    }

    private URI jwksUri() {
        return URI.create(properties.jwksUri() == null || properties.jwksUri().isBlank() ? DEFAULT_JWKS_URI : properties.jwksUri());
    }

    private String issuer() {
        return properties.issuer() == null || properties.issuer().isBlank() ? DEFAULT_ISSUER : properties.issuer();
    }

}
