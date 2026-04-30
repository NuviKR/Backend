package com.nuvi.nuvi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.domain.repository.OidcMemberRepository;
import com.nuvi.nuvi.auth.infra.adapter.KakaoOidcClaims;
import com.nuvi.nuvi.auth.infra.adapter.KakaoOidcClient;
import com.nuvi.nuvi.auth.infra.adapter.KakaoOidcClientException;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.SupplementContextInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nuvi.auth.kakao.rest-api-key=test_kakao_rest_api_key")
@AutoConfigureMockMvc
class ApiSkeletonEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OidcMemberRepository oidcMemberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void kakaoAuthorizeUsesApiResponseEnvelopeAndOpenIdScope() throws Exception {
        mockMvc.perform(get("/api/v1/auth/kakao/authorize")
                        .param("redirectUri", "http://localhost:3000/auth/callback")
                        .param("state", "state_1234567890")
                        .header("X-Request-Id", "req_test_auth"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req_test_auth"))
                .andExpect(jsonPath("$.data.authorizeUrl").exists())
                .andExpect(jsonPath("$.data.authorizeUrl").value(org.hamcrest.Matchers.containsString("client_id=test_kakao_rest_api_key")))
                .andExpect(jsonPath("$.data.authorizeUrl").value(org.hamcrest.Matchers.containsString("scope=openid")))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_auth"));
    }

    @Test
    void kakaoCallbackMapsVerifiedOidcSubjectToMemberSkeleton() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "valid_code_123",
                                  "state": "state_1234567890",
                                  "redirectUri": "http://localhost:3000/auth/callback"
                                }
                                """)
                        .header("X-Request-Id", "req_test_kakao_callback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.session.authenticated").value(true))
                .andExpect(jsonPath("$.data.session.memberId").value(org.hamcrest.Matchers.startsWith("mem_")))
                .andExpect(jsonPath("$.data.session.provider").value("KAKAO"))
                .andExpect(jsonPath("$.data.session.emailAuthEnabled").value(false))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_kakao_callback"));
    }

    @Test
    void kakaoCallbackInvalidCodeUsesStandardErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "invalid_code",
                                  "state": "state_1234567890",
                                  "redirectUri": "http://localhost:3000/auth/callback"
                                }
                                """)
                        .header("X-Request-Id", "req_test_kakao_invalid_code"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("Kakao OIDC authentication failed."))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_kakao_invalid_code"));
    }

    @Test
    void kakaoCallbackUnverifiableIdTokenUsesStandardErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "invalid_id_token",
                                  "state": "state_1234567890",
                                  "redirectUri": "http://localhost:3000/auth/callback"
                                }
                                """)
                        .header("X-Request-Id", "req_test_kakao_invalid_id_token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("Kakao OIDC authentication failed."))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_kakao_invalid_id_token"));
    }

    @Test
    void emailLoginRemainsDisabledForBeta() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "tester@example.com",
                                  "password": "password123"
                                }
                                """)
                        .header("X-Request-Id", "req_test_email"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("EMAIL_AUTH_DISABLED"))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_email"));
    }

    @Test
    void kakaoCallbackValidationFailureUsesErrorEnvelopeWithRequestId() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("X-Request-Id", "req_test_auth_validation"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "req_test_auth_validation"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.meta.requestId").value("req_test_auth_validation"));
    }

    @Test
    void onboardingProfileCreateUsesDataMetaEnvelope() throws Exception {
        String memberId = oidcMemberRepository.findOrCreate(AuthProvider.KAKAO, "kakao_onboarding_skeleton").memberId();

        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ageRange": "AGE_30_39",
                                  "householdSize": 1,
                                  "weeklyBudget": {
                                    "amount": 100000,
                                    "currency": "KRW"
                                  },
                                  "goals": ["BALANCED_NUTRITION"],
                                  "allergies": [],
                                  "preferences": {
                                    "excludedIngredientCodes": [],
                                    "preferredIngredientCodes": [],
                                    "dietTypes": []
                                  },
                                  "supplementContext": {
                                    "usesSupplements": false,
                                    "supplementIngredientCodes": [],
                                    "usesMedication": false,
                                    "expertConsultationNoticeAccepted": false
                                  }
                                }
                                """)
                        .header("X-Nuvi-Member-Id", memberId)
                        .header("X-Request-Id", "req_test_onboarding"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.supplementContext.usesMedication").value(false))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_onboarding"));
    }

    @Test
    void onboardingValidationRejectsInvalidCreateRequestWithRequestId() throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ageRange": "AGE_30_39",
                                  "householdSize": 0,
                                  "weeklyBudget": {
                                    "amount": -1,
                                    "currency": "USD"
                                  },
                                  "goals": [],
                                  "allergies": [
                                    {
                                      "allergenCode": "",
                                      "severity": null
                                    }
                                  ],
                                  "preferences": {
                                    "excludedIngredientCodes": [],
                                    "preferredIngredientCodes": [],
                                    "dietTypes": []
                                  },
                                  "supplementContext": {
                                    "usesSupplements": false,
                                    "supplementIngredientCodes": [],
                                    "usesMedication": false,
                                    "expertConsultationNoticeAccepted": false
                                  }
                                }
                                """)
                        .header("X-Request-Id", "req_test_onboarding_validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details").isNotEmpty())
                .andExpect(jsonPath("$.meta.requestId").value("req_test_onboarding_validation"));
    }

    @Test
    void weeklyCartCreateUsesOpenApiEnvelopeAndSafetyShape() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/carts/weekly")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "weekStartDate": "2026-04-27"
                                }
                                """)
                        .header("Idempotency-Key", "idem_weekly_cart_123")
                        .header("X-Request-Id", "req_test_cart"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("cart_skeleton"))
                .andExpect(jsonPath("$.data.items[0].id").value("citem_skeleton"))
                .andExpect(jsonPath("$.data.safetySummary.medicationInputAccepted").value(false))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_cart"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        assertThat(json.has("error")).isFalse();
    }

    @Test
    void weeklyCartMutationRequiresIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/carts/cart_skeleton/refresh")
                        .header("X-Request-Id", "req_test_missing_idem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_missing_idem"));
    }

    @Test
    void cartItemUpdateValidationRejectsEmptyPatchWithRequestId() throws Exception {
        mockMvc.perform(patch("/api/v1/carts/cart_skeleton/items/citem_skeleton")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Idempotency-Key", "idem_cart_update_123")
                        .header("X-Request-Id", "req_test_cart_validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details").isNotEmpty())
                .andExpect(jsonPath("$.meta.requestId").value("req_test_cart_validation"));
    }

    @Test
    void cartItemExcludeUsesPostEndpointForV03SpecSync() throws Exception {
        mockMvc.perform(post("/api/v1/carts/cart_skeleton/items/citem_skeleton/exclude")
                        .header("Idempotency-Key", "idem_cart_exclude_123")
                        .header("X-Request-Id", "req_test_cart_exclude"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("cart_skeleton"))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_cart_exclude"));
    }

    @Test
    void swaggerUiAndOpenApiDocsAreExposed() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header("X-Request-Id", "req_test_openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/v1/carts/weekly']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/carts/{cartId}/items/{cartItemId}/exclude']").exists());

        int swaggerStatus = mockMvc.perform(get("/swagger-ui.html"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(swaggerStatus).isIn(200, 302);
    }

    @Test
    void supplementContextDoesNotAcceptMedicationDetailsAsModeledFields() {
        var componentNames = Arrays.stream(SupplementContextInput.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(componentNames)
                .containsExactly(
                        "usesSupplements",
                        "supplementIngredientCodes",
                        "usesMedication",
                        "expertConsultationNoticeAccepted"
                );
        assertThat(componentNames).doesNotContain("medicationName", "dosage", "dose", "schedule");
    }

    @TestConfiguration
    static class KakaoOidcTestConfiguration {

        @Bean
        @Primary
        KakaoOidcClient kakaoOidcClient() {
            return (code, redirectUri) -> {
                if ("invalid_code".equals(code)) {
                    throw new KakaoOidcClientException("Invalid authorization code.");
                }
                if ("invalid_id_token".equals(code)) {
                    throw new KakaoOidcClientException("The id_token could not be verified.");
                }
                String subject = switch (code) {
                    case "valid_code_new_subject" -> "kakao_oidc_subject_456";
                    default -> "kakao_oidc_subject_123";
                };
                return new KakaoOidcClaims(
                        subject,
                        "https://kauth.kakao.com",
                        "test_kakao_rest_api_key",
                        Instant.parse("2099-01-01T00:00:00Z")
                );
            };
        }
    }
}
