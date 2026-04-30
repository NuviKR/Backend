package com.nuvi.nuvi.auth.infra.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;

record KakaoTokenResponse(
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("id_token")
        String idToken,
        @JsonProperty("expires_in")
        Long expiresIn
) {
}
