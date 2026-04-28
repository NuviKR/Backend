package com.nuvi.nuvi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvi.nuvi.onboarding.controller.dto.OnboardingDtos.SupplementContextInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSkeletonEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void kakaoAuthorizeUsesApiResponseEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/auth/kakao/authorize")
                        .param("redirectUri", "http://localhost:3000/auth/callback")
                        .param("state", "state_1234567890")
                        .header("X-Request-Id", "req_test_auth"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req_test_auth"))
                .andExpect(jsonPath("$.data.authorizeUrl").exists())
                .andExpect(jsonPath("$.meta.requestId").value("req_test_auth"));
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
    void onboardingProfileCreateUsesDataMetaEnvelope() throws Exception {
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
                        .header("X-Request-Id", "req_test_onboarding"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.supplementContext.usesMedication").value(false))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_onboarding"));
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
}
