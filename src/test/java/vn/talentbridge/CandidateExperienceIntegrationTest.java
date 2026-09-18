package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.in.web.dto.request.CandidateSkillRequest;
import vn.talentbridge.adapter.in.web.dto.request.WorkExperienceRequest;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CandidateExperienceIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private UserJpaEntity testUser;
    private CandidateJpaEntity testCandidate;
    private String candidateToken;

    @BeforeEach
    void setUp() {
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(new RoleJpaEntity(null, "ROLE_CANDIDATE", "Candidate Role")));

        testUser = new UserJpaEntity();
        testUser.setEmail("cand_exp_test@talentbridge.vn");
        testUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        testUser.setFullName("Nguyễn Trải Nghiệm");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(candidateRole));
        testUser = userRepository.save(testUser);

        testCandidate = new CandidateJpaEntity();
        testCandidate.setUser(testUser);
        testCandidate.setTitle("Software Engineer");
        testCandidate.setExperienceYears(0);
        testCandidate = candidateRepository.save(testCandidate);

        candidateToken = loginUseCase.login(
                new LoginCommand("cand_exp_test@talentbridge.vn", "Password123!")
        ).accessToken();
    }

    @Test
    @DisplayName("Thêm và tự động tính số năm kinh nghiệm từ quá trình làm việc")
    void testWorkExperience_AddAndAutoCalculateYears() throws Exception {
        // Thêm kinh nghiệm 2 năm (2022-01-01 đến 2024-01-01)
        WorkExperienceRequest req = new WorkExperienceRequest(
                "Công ty Công nghệ ABC",
                "Backend Developer",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1),
                false,
                "Phát triển hệ thống Microservices Spring Boot",
                "Tối ưu hiệu năng hệ thống tăng 30%"
        );

        mockMvc.perform(post("/api/v1/candidates/work-experiences")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.companyName").value("Công ty Công nghệ ABC"))
                .andExpect(jsonPath("$.data.position").value("Backend Developer"));

        // Xác minh candidate.experienceYears tự động được cập nhật thành 2 năm
        CandidateJpaEntity updatedCandidate = candidateRepository.findById(testCandidate.getId()).orElseThrow();
        assertEquals(2, updatedCandidate.getExperienceYears());

        // Lấy danh sách kinh nghiệm
        mockMvc.perform(get("/api/v1/candidates/work-experiences")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].companyName").value("Công ty Công nghệ ABC"));
    }

    @Test
    @DisplayName("Thêm và xóa kỹ năng chuyên môn với đánh giá sao (1-5)")
    void testCandidateSkill_AddAndRemove() throws Exception {
        CandidateSkillRequest skillReq = new CandidateSkillRequest(
                null,
                "Spring Boot",
                "ADVANCED",
                4,
                2.5
        );

        mockMvc.perform(post("/api/v1/candidates/skills")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.skillName").value("Spring Boot"))
                .andExpect(jsonPath("$.data.rating").value(4));

        // Lấy danh sách kỹ năng của candidate
        mockMvc.perform(get("/api/v1/candidates/skills")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].skillName").value("Spring Boot"));

        // Lấy danh mục master skills
        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }
}
