package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.in.security.AuthRateLimitFilter;
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "talentbridge.rate-limit.enabled=true",
        "talentbridge.rate-limit.auth-limit=5",
        "talentbridge.rate-limit.window-seconds=60"
})
class AuthRateLimitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthRateLimitFilter authRateLimitFilter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authRateLimitFilter.reset();
    }

    @Test
    @DisplayName("Should return 429 Too Many Requests when rate limit threshold is exceeded for auth login endpoint")
    void testRateLimitExceededOnLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("ratelimit@talentbridge.vn");
        request.setPassword("InvalidPassword123!");

        String json = objectMapper.writeValueAsString(request);

        // First 5 requests should reach controller (returning 401 for wrong credentials)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "192.168.1.100")
                            .content(json))
                    .andExpect(status().isUnauthorized());
        }

        // 6th request from the same IP should be blocked by rate limiter with 429
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "192.168.1.100")
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.statusCode").value(42901));
    }

    @Test
    @DisplayName("Requests from a different IP should not be blocked when one IP is rate limited")
    void testDifferentIpNotBlocked() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("ratelimit2@talentbridge.vn");
        request.setPassword("InvalidPassword123!");

        String json = objectMapper.writeValueAsString(request);

        // Exhaust limit for IP 10.0.0.1
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "10.0.0.1")
                            .content(json))
                    .andExpect(status().isUnauthorized());
        }

        // Another IP 10.0.0.2 should still succeed / reach controller
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "10.0.0.2")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ForgotPassword endpoint should also be protected by rate limiting")
    void testForgotPasswordRateLimiting() throws Exception {
        String forgotJson = "{\"email\":\"forgot-rate@talentbridge.vn\"}";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "192.168.2.50")
                            .content(forgotJson))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "192.168.2.50")
                        .content(forgotJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.statusCode").value(42901));
    }
}
