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
import vn.talentbridge.adapter.in.web.dto.request.RegisterRequest;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterRoleValidationIntegrationTest {

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

    @BeforeEach
    @AfterEach
    void cleanup() {
        candidateJpaRepository.deleteAll();
        recruiterJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid role returns 400 Bad Request with code 40005")
    void register_withInvalidRole_returnsBadRequestWithErrorCode40005() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalidrole@talentbridge.vn")
                .password("Password123!")
                .fullName("Invalid Role User")
                .phone("0912345678")
                .role("INVALID_ROLE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40005))
                .andExpect(jsonPath("$.message", containsString("Vai trò không hợp lệ")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with ROLE_ADMIN returns 400 Bad Request with code 40005")
    void register_withRoleAdmin_returnsBadRequestWithErrorCode40005() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("adminwannabe@talentbridge.vn")
                .password("Password123!")
                .fullName("Admin Wannabe")
                .phone("0912345678")
                .role("ROLE_ADMIN")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40005))
                .andExpect(jsonPath("$.message", containsString("Vai trò không hợp lệ")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with ROLE_CANDIDATE succeeds and creates candidate profile")
    void register_withRoleCandidate_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("candidate-valid@talentbridge.vn")
                .password("Password123!")
                .fullName("Valid Candidate")
                .phone("0912345678")
                .role("ROLE_CANDIDATE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_CANDIDATE"));
    }
}
