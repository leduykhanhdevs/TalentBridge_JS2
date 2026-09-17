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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.in.web.dto.request.UpdateCandidateProfileRequest;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RoleJpaRepository roleRepository;

    @Autowired
    private CandidateJpaRepository candidateRepository;

    @Autowired
    private vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository recruiterRepository;

    @Autowired
    private vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository companyRepository;

    @Autowired
    private vn.talentbridge.adapter.out.persistence.repository.AuthSessionJpaRepository authSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private String candidateToken;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        cleanup();

        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_CANDIDATE").build()));

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));

        // 1. Create candidate user
        UserJpaEntity candidateUser = UserJpaEntity.builder()
                .email("candidate_test@domain.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Trần Ứng Viên")
                .phoneNumber("0911223344")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(candidateRole)))
                .build();
        candidateUser = userRepository.save(candidateUser);

        CandidateJpaEntity candidate = CandidateJpaEntity.builder()
                .user(candidateUser)
                .title("Junior Java Developer")
                .experienceYears(1)
                .city("Hà Nội")
                .build();
        candidateRepository.save(candidate);

        candidateToken = loginUseCase.login(
                new LoginCommand("candidate_test@domain.com", "Password123!")
        ).accessToken();

        // 2. Create recruiter user
        UserJpaEntity recruiterUser = UserJpaEntity.builder()
                .email("recruiter_test@domain.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Lê Nhà Tuyển Dụng")
                .phoneNumber("0922334455")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        recruiterUser = userRepository.save(recruiterUser);

        recruiterToken = loginUseCase.login(
                new LoginCommand("recruiter_test@domain.com", "Password123!")
        ).accessToken();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        candidateRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        authSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/candidates/profile - Lấy hồ sơ ứng viên thành công")
    void testGetProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/profile")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.fullName").value("Trần Ứng Viên"))
                .andExpect(jsonPath("$.data.email").value("candidate_test@domain.com"))
                .andExpect(jsonPath("$.data.title").value("Junior Java Developer"))
                .andExpect(jsonPath("$.data.experienceYears").value(1))
                .andExpect(jsonPath("$.data.city").value("Hà Nội"));
    }

    @Test
    @DisplayName("PUT /api/v1/candidates/profile - Cập nhật hồ sơ ứng viên thành công")
    void testUpdateProfileSuccess() throws Exception {
        UpdateCandidateProfileRequest request = UpdateCandidateProfileRequest.builder()
                .fullName("Trần Ứng Viên Pro")
                .phone("0988776655")
                .title("Senior Backend Engineer")
                .dob(LocalDate.of(1998, 10, 20))
                .gender("NAM")
                .summary("Chuyên gia Spring Boot và Cloud Architecture với hơn 5 năm kinh nghiệm.")
                .experienceYears(5)
                .currentSalary(new BigDecimal("25000000"))
                .expectedSalary(new BigDecimal("40000000"))
                .city("TP. Hồ Chí Minh")
                .address("123 Nguyễn Thị Minh Khai, Quận 1")
                .githubUrl("https://github.com/ungvienpro")
                .linkedinUrl("https://linkedin.com/in/ungvienpro")
                .personalWebsite("https://ungvienpro.dev")
                .build();

        mockMvc.perform(put("/api/v1/candidates/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.fullName").value("Trần Ứng Viên Pro"))
                .andExpect(jsonPath("$.data.phone").value("0988776655"))
                .andExpect(jsonPath("$.data.title").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.data.experienceYears").value(5))
                .andExpect(jsonPath("$.data.city").value("TP. Hồ Chí Minh"))
                .andExpect(jsonPath("$.data.githubUrl").value("https://github.com/ungvienpro"));
    }

    @Test
    @DisplayName("PUT /api/v1/candidates/profile - Lương âm hoặc kinh nghiệm âm trả về 400 Bad Request")
    void testUpdateProfileNegativeSalaryOrExperience() throws Exception {
        UpdateCandidateProfileRequest request = UpdateCandidateProfileRequest.builder()
                .experienceYears(-1)
                .build();

        mockMvc.perform(put("/api/v1/candidates/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/candidates/profile - Chưa đăng nhập trả về 401 Unauthorized")
    void testGetProfileUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/candidates/profile - Vai trò Recruiter bị từ chối 403 Forbidden")
    void testGetProfileForbiddenForRecruiter() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/profile")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }
}
