package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;
import vn.talentbridge.adapter.in.web.dto.request.RegisterRequest;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.LoginAttemptTrackerPort;
import vn.talentbridge.core.domain.vo.UserStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginLockoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CandidateJpaRepository candidateJpaRepository;

    @Autowired
    private RecruiterJpaRepository recruiterJpaRepository;

    @Autowired
    private JobJpaRepository jobJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private LoginAttemptTrackerPort loginAttemptTracker;

    private static final String TEST_EMAIL = "lockout-test@talentbridge.vn";
    private static final String CORRECT_PASSWORD = "CorrectPassword123!";
    private static final String WRONG_PASSWORD = "WrongPassword999!";

    @BeforeEach
    void setUp() throws Exception {
        cleanup();
        loginAttemptTracker.resetAttempts(TEST_EMAIL);

        // Register a fresh user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(TEST_EMAIL)
                .password(CORRECT_PASSWORD)
                .fullName("Lockout Test User")
                .role("ROLE_CANDIDATE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void tearDown() {
        loginAttemptTracker.resetAttempts(TEST_EMAIL);
        cleanup();
    }

    private void cleanup() {
        candidateJpaRepository.deleteAll();
        recruiterJpaRepository.deleteAll();
        jobJpaRepository.deleteAll();
        companyJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("HRPM-10: 4 failed login attempts return 401 and account remains ACTIVE")
    void failedAttempts_upTo4Times_returns401AndRemainsActive() throws Exception {
        LoginRequest wrongRequest = new LoginRequest(TEST_EMAIL, WRONG_PASSWORD);

        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.statusCode").value(40102));

            assertEquals(i, loginAttemptTracker.getAttempts(TEST_EMAIL));
        }

        UserJpaEntity user = userJpaRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("HRPM-10: 5th failed login attempt locks account and returns 403 Forbidden")
    void fifthFailedAttempt_locksAccountAndReturns403() throws Exception {
        LoginRequest wrongRequest = new LoginRequest(TEST_EMAIL, WRONG_PASSWORD);

        // 4 failed attempts
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 5th failed attempt -> account is locked
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(40301));

        UserJpaEntity lockedUser = userJpaRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(UserStatus.LOCKED, lockedUser.getStatus());

        // Subsequent attempt with correct password must still be rejected with 403 Forbidden
        LoginRequest correctRequest = new LoginRequest(TEST_EMAIL, CORRECT_PASSWORD);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(40301));
    }

    @Test
    @DisplayName("Successful login resets failed attempt counter")
    void successfulLogin_resetsFailedAttempts() throws Exception {
        LoginRequest wrongRequest = new LoginRequest(TEST_EMAIL, WRONG_PASSWORD);
        LoginRequest correctRequest = new LoginRequest(TEST_EMAIL, CORRECT_PASSWORD);

        // Fail 2 times
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isUnauthorized());

        assertEquals(2, loginAttemptTracker.getAttempts(TEST_EMAIL));

        // Succeed once
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isOk());

        // Tracker should be reset
        assertEquals(0, loginAttemptTracker.getAttempts(TEST_EMAIL));

        UserJpaEntity user = userJpaRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
}
