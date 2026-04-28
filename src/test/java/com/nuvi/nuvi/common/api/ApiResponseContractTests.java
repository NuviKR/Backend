package com.nuvi.nuvi.common.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseContractTests {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void successResponseUsesDataAndMetaEnvelope() {
        ApiResponse<Map<String, String>> response = ApiResponse.ok(
                Map.of("id", "cart_123"),
                new Meta("req_test", Instant.parse("2026-04-28T00:00:00Z"))
        );

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("data")).isTrue();
        assertThat(json.has("meta")).isTrue();
        assertThat(json.has("error")).isFalse();
        assertThat(json.at("/data/id").asText()).isEqualTo("cart_123");
        assertThat(json.at("/meta/requestId").asText()).isEqualTo("req_test");
    }

    @Test
    void errorResponseUsesErrorAndMetaEnvelope() {
        ErrorResponse response = ErrorResponse.of(
                ApiError.of(ApiErrorCode.ALLERGY_CONFLICT, "Allergy conflict."),
                new Meta("req_error", Instant.parse("2026-04-28T00:00:00Z"))
        );

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("error")).isTrue();
        assertThat(json.has("meta")).isTrue();
        assertThat(json.has("data")).isFalse();
        assertThat(json.at("/error/code").asText()).isEqualTo("ALLERGY_CONFLICT");
        assertThat(json.at("/error/message").asText()).isEqualTo("Allergy conflict.");
        assertThat(json.at("/error/details").isArray()).isTrue();
        assertThat(json.at("/meta/requestId").asText()).isEqualTo("req_error");
    }

    @Test
    void errorCodesMatchOpenApiDraft() {
        List<String> expectedCodes = List.of(
                "VALIDATION_FAILED",
                "AUTH_REQUIRED",
                "FORBIDDEN",
                "RATE_LIMITED",
                "FEATURE_DISABLED",
                "EMAIL_AUTH_DISABLED",
                "CONSENT_REQUIRED",
                "PROFILE_INCOMPLETE",
                "ALLERGY_CONFLICT",
                "ALLERGY_MAPPING_INCOMPLETE",
                "SUPPLEMENT_RULE_CONFLICT",
                "SUPPLEMENT_INPUT_INVALID",
                "ANAPHYLACTIC_BLOCK",
                "PRODUCT_UNAVAILABLE",
                "PARTNER_API_UNAVAILABLE",
                "PARTNER_OFFER_EXPIRED",
                "CART_NOT_FOUND",
                "STALE_CART_NEEDS_REFRESH",
                "BUDGET_EXCEEDED_NO_ALTERNATIVES",
                "MEAL_PLAN_NOT_FOUND",
                "RECOMMENDATION_FAILED",
                "IDEMPOTENCY_KEY_CONFLICT",
                "INTERNAL_ERROR"
        );

        List<String> actualCodes = Arrays.stream(ApiErrorCode.values())
                .map(Enum::name)
                .toList();

        assertThat(actualCodes).containsExactlyElementsOf(expectedCodes);
    }

    @Test
    void requestIdFilterPropagatesSafeClientRequestId() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/carts/current");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestId.HEADER_NAME, "req_client_123");
        AtomicReference<String> requestIdDuringChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                requestIdDuringChain.set(RequestId.current().orElseThrow());

        filter.doFilter(request, response, chain);

        assertThat(requestIdDuringChain).hasValue("req_client_123");
        assertThat(response.getHeader(RequestId.HEADER_NAME)).isEqualTo("req_client_123");
        assertThat(RequestId.current()).isEmpty();
    }

    @Test
    void requestIdFilterGeneratesRequestIdWhenHeaderIsUnsafe() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/carts/current");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestId.HEADER_NAME, "not safe whitespace");
        AtomicReference<String> requestIdDuringChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                requestIdDuringChain.set(RequestId.current().orElseThrow());

        filter.doFilter(request, response, chain);

        assertThat(requestIdDuringChain.get()).startsWith("req_");
        assertThat(response.getHeader(RequestId.HEADER_NAME)).isEqualTo(requestIdDuringChain.get());
        assertThat(RequestId.current()).isEmpty();
    }
}
