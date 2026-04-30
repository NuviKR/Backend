package com.nuvi.nuvi.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvi.nuvi.auth.domain.model.AuthProvider;
import com.nuvi.nuvi.auth.domain.repository.OidcMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OnboardingProfilePersistenceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OidcMemberRepository oidcMemberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createGetPatchAndCompletePersistProfileByMemberId() throws Exception {
        String memberId = createKakaoMember("kakao_profile_crud");

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
                                  "allergies": [
                                    {
                                      "allergenCode": "PEANUT",
                                      "severity": "ALLERGY"
                                    }
                                  ],
                                  "preferences": {
                                    "excludedIngredientCodes": ["SHELLFISH"],
                                    "preferredIngredientCodes": ["TOFU"],
                                    "dietTypes": ["HIGH_PROTEIN"]
                                  },
                                  "supplementContext": {
                                    "usesSupplements": true,
                                    "supplementIngredientCodes": ["VITAMIN_D"],
                                    "usesMedication": false,
                                    "expertConsultationNoticeAccepted": false
                                  }
                                }
                                """)
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.allergies[0].allergenCode").value("PEANUT"));

        mockMvc.perform(get("/api/v1/onboarding/profile")
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weeklyBudget.amount").value(100000))
                .andExpect(jsonPath("$.data.preferences.excludedIngredientCodes[0]").value("SHELLFISH"))
                .andExpect(jsonPath("$.data.supplementContext.supplementIngredientCodes[0]").value("VITAMIN_D"));

        mockMvc.perform(patch("/api/v1/onboarding/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdSize": 2,
                                  "goals": ["ENERGY_SUPPORT", "BUDGET_OPTIMIZATION"],
                                  "allergies": [
                                    {
                                      "allergenCode": "MILK",
                                      "severity": "INTOLERANCE"
                                    }
                                  ]
                                }
                                """)
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.householdSize").value(2))
                .andExpect(jsonPath("$.data.allergies[0].allergenCode").value("MILK"))
                .andExpect(jsonPath("$.data.allergies[0].severity").value("INTOLERANCE"));

        mockMvc.perform(post("/api/v1/onboarding/complete")
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.nextAction").value("GENERATE_WEEKLY_CART"));

        assertThat(queryString("select status from user_profiles where member_id = ?", memberId)).isEqualTo("COMPLETE");
        assertThat(queryInteger("select count(*) from health_goals where member_id = ?", memberId)).isEqualTo(2);
        assertThat(queryInteger("select count(*) from allergies where member_id = ?", memberId)).isEqualTo(1);
    }

    @Test
    void storesAndReadsAllFourAllergySeverityValues() throws Exception {
        String memberId = createKakaoMember("kakao_profile_allergy_severities");

        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ageRange": "AGE_40_49",
                                  "householdSize": 1,
                                  "weeklyBudget": {
                                    "amount": 150000,
                                    "currency": "KRW"
                                  },
                                  "goals": ["BALANCED_NUTRITION"],
                                  "allergies": [
                                    {
                                      "allergenCode": "ONION",
                                      "severity": "MILD_PREFERENCE"
                                    },
                                    {
                                      "allergenCode": "MILK",
                                      "severity": "INTOLERANCE"
                                    },
                                    {
                                      "allergenCode": "PEANUT",
                                      "severity": "ALLERGY"
                                    },
                                    {
                                      "allergenCode": "SHELLFISH",
                                      "severity": "ANAPHYLACTIC"
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
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(get("/api/v1/onboarding/profile")
                        .header("X-Nuvi-Member-Id", memberId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode allergies = objectMapper.readTree(response).path("data").path("allergies");
        assertThat(allergies.findValuesAsText("severity"))
                .containsExactly("INTOLERANCE", "MILD_PREFERENCE", "ALLERGY", "ANAPHYLACTIC");
        assertThat(queryInteger("select count(*) from allergies where member_id = ? and severity = 'MILD_PREFERENCE'", memberId)).isEqualTo(1);
        assertThat(queryInteger("select count(*) from allergies where member_id = ? and severity = 'INTOLERANCE'", memberId)).isEqualTo(1);
        assertThat(queryInteger("select count(*) from allergies where member_id = ? and severity = 'ALLERGY'", memberId)).isEqualTo(1);
        assertThat(queryInteger("select count(*) from allergies where member_id = ? and severity = 'ANAPHYLACTIC'", memberId)).isEqualTo(1);
    }

    @Test
    void h2AppliesFlywayBaselineMigration() {
        assertThat(queryInteger("select count(*) from \"flyway_schema_history\"")).isGreaterThanOrEqualTo(1);
        assertThat(queryInteger("select count(*) from information_schema.tables where table_name = 'USER_PROFILES'")).isEqualTo(1);
        assertThat(queryInteger("select count(*) from information_schema.tables where table_name = 'ALLERGIES'")).isEqualTo(1);
    }

    private String createKakaoMember(String subject) {
        return oidcMemberRepository.findOrCreate(AuthProvider.KAKAO, subject).memberId();
    }

    private Integer queryInteger(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

    private String queryString(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, String.class, args);
    }
}
