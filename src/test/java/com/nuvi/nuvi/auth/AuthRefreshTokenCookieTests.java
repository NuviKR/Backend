package com.nuvi.nuvi.auth;

import com.nuvi.nuvi.auth.infra.KakaoOidcClaims;
import com.nuvi.nuvi.auth.infra.KakaoOidcClient;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nuvi.auth.kakao.rest-api-key=test_kakao_rest_api_key")
@AutoConfigureMockMvc
class AuthRefreshTokenCookieTests {

    private static final String REFRESH_COOKIE = "nuvi_refresh_token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void kakaoCallbackIssuesRefreshTokenAsHttpOnlyCookie() throws Exception {
        String setCookie = mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kakaoCallbackBody("valid_code_cookie_issue"))
                        .header("X-Request-Id", "req_test_refresh_cookie_issue"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.data.refreshToken").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.session.authenticated").value(true))
                .andExpect(jsonPath("$.data.session.emailAuthEnabled").value(false))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie)
                .contains(REFRESH_COOKIE + "=")
                .contains("HttpOnly")
                .contains("Path=/api/v1/auth")
                .contains("SameSite=Lax");
    }

    @Test
    void refreshEndpointRotatesRefreshTokenCookie() throws Exception {
        String firstToken = extractRefreshToken(loginAndReadSetCookie("valid_code_cookie_rotate"));

        String rotatedSetCookie = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE, firstToken))
                        .header("X-Request-Id", "req_test_refresh_rotate"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.data.accessToken").value("access_skeleton_refreshed"))
                .andExpect(jsonPath("$.data.refreshToken").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.session.memberId").value(org.hamcrest.Matchers.startsWith("mem_")))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        String rotatedToken = extractRefreshToken(rotatedSetCookie);
        assertThat(rotatedToken).isNotEqualTo(firstToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE, firstToken))
                        .header("X-Request-Id", "req_test_refresh_reuse_old"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_refresh_reuse_old"));
    }

    @Test
    void logoutRevokesRefreshTokenAndClearsCookie() throws Exception {
        String refreshToken = extractRefreshToken(loginAndReadSetCookie("valid_code_cookie_logout"));

        String deleteCookie = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie(REFRESH_COOKIE, refreshToken))
                        .header("X-Request-Id", "req_test_logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_logout"))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        assertThat(deleteCookie)
                .contains(REFRESH_COOKIE + "=")
                .contains("Max-Age=0")
                .contains("HttpOnly");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE, refreshToken))
                        .header("X-Request-Id", "req_test_refresh_after_logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.meta.requestId").value("req_test_refresh_after_logout"));
    }

    private String loginAndReadSetCookie(String code) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/kakao/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kakaoCallbackBody(code))
                        .header("X-Request-Id", "req_test_login_" + code))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);
    }

    private static String kakaoCallbackBody(String code) {
        return """
                {
                  "code": "%s",
                  "state": "state_1234567890",
                  "redirectUri": "http://localhost:3000/auth/callback"
                }
                """.formatted(code);
    }

    private static String extractRefreshToken(String setCookie) {
        assertThat(setCookie).isNotBlank();
        String prefix = REFRESH_COOKIE + "=";
        int start = setCookie.indexOf(prefix);
        assertThat(start).isGreaterThanOrEqualTo(0);
        int valueStart = start + prefix.length();
        int valueEnd = setCookie.indexOf(';', valueStart);
        return setCookie.substring(valueStart, valueEnd);
    }

    @TestConfiguration
    static class KakaoOidcTestConfiguration {

        @Bean
        @Primary
        KakaoOidcClient kakaoOidcClient() {
            return (code, redirectUri) -> new KakaoOidcClaims(
                    "kakao_oidc_subject_" + code,
                    "https://kauth.kakao.com",
                    "test_kakao_rest_api_key",
                    Instant.parse("2099-01-01T00:00:00Z")
            );
        }
    }
}
