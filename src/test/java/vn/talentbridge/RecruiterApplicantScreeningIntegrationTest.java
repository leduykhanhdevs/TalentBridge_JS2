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
import vn.talentbridge.adapter.in.web.dto.request.RateAndNoteApplicantRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateApplicantStatusRequest;
import vn.talentbridge.adapter.out.persistence.entity.*;
import vn.talentbridge.adapter.out.persistence.repository.*;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecruiterApplicantScreeningIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private RoleJpaRepository roleRepository;
    @Autowired private RecruiterJpaRepository recruiterRepository;
    @Autowired private CandidateJpaRepository candidateRepository;
    @Autowired private CompanyJpaRepository companyRepository;
    @Autowired private JobJpaRepository jobRepository;
    @Autowired private ResumeJpaRepository resumeRepository;
    @Autowired private ApplicationJpaRepository applicationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private LoginUseCase loginUseCase;

    private String recruiterToken;
    private String otherRecruiterToken;
    private JobJpaEntity job;
    private ApplicationJpaEntity app1;
    private ApplicationJpaEntity app2;

    @BeforeEach
    void setUp() {
        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(new RoleJpaEntity(null, "ROLE_RECRUITER", "Recruiter")));

        // Company 1
        CompanyJpaEntity company1 = companyRepository.save(CompanyJpaEntity.builder()
                .name("VinAI Screening Corp")
                .status(CompanyStatus.APPROVED)
                .city("Hà Nội")
                .address("Keangnam Tower")
                .build());

        // Recruiter 1 in Company 1
        UserJpaEntity recUser1 = userRepository.save(UserJpaEntity.builder()
                .email("hr_screening1@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Trần Đình Tình HR")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(recruiterRole))
                .build());

        recruiterRepository.save(RecruiterJpaEntity.builder()
                .user(recUser1)
                .company(company1)
                .position("Talent Acquisition Lead")
                .build());

        // Company 2 and Recruiter 2
        CompanyJpaEntity company2 = companyRepository.save(CompanyJpaEntity.builder()
                .name("Other Tech Corp")
                .status(CompanyStatus.APPROVED)
                .city("TP. Hồ Chí Minh")
                .build());

        UserJpaEntity recUser2 = userRepository.save(UserJpaEntity.builder()
                .email("hr_other@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Other HR")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(recruiterRole))
                .build());

        recruiterRepository.save(RecruiterJpaEntity.builder()
                .user(recUser2)
                .company(company2)
                .position("HR Generalist")
                .build());

        // Candidate 1
        UserJpaEntity candUser1 = userRepository.save(UserJpaEntity.builder()
                .email("candidate_alice@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Nguyen Thi Alice")
                .phoneNumber("0912345678")
                .status(UserStatus.ACTIVE)
                .build());

        CandidateJpaEntity cand1 = candidateRepository.save(CandidateJpaEntity.builder()
                .user(candUser1)
                .title("Senior Backend Developer")
                .experienceYears(5)
                .city("Hà Nội")
                .build());

        ResumeJpaEntity resume1 = resumeRepository.save(ResumeJpaEntity.builder()
                .candidate(cand1)
                .title("Alice CV Backend")
                .fileName("alice_cv.pdf")
                .fileUrl("https://storage.talentbridge.vn/resumes/alice.pdf")
                .isDefault(true)
                .build());

        // Candidate 2
        UserJpaEntity candUser2 = userRepository.save(UserJpaEntity.builder()
                .email("candidate_bob@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Tran Van Bob")
                .phoneNumber("0987654321")
                .status(UserStatus.ACTIVE)
                .build());

        CandidateJpaEntity cand2 = candidateRepository.save(CandidateJpaEntity.builder()
                .user(candUser2)
                .title("Junior Java Developer")
                .experienceYears(1)
                .city("Đà Nẵng")
                .build());

        ResumeJpaEntity resume2 = resumeRepository.save(ResumeJpaEntity.builder()
                .candidate(cand2)
                .title("Bob CV Junior")
                .fileName("bob_cv.pdf")
                .fileUrl("https://storage.talentbridge.vn/resumes/bob.pdf")
                .isDefault(true)
                .build());

        // Job in Company 1
        job = jobRepository.save(JobJpaEntity.builder()
                .company(company1)
                .title("Senior Spring Boot Engineer")
                .description("Tuyển dụng kỹ sư Spring Boot cấp cao cho dự án lớn")
                .requirements("Có từ 3 năm kinh nghiệm với Java Spring")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusMonths(1))
                .build());

        // Applications
        app1 = applicationRepository.save(ApplicationJpaEntity.builder()
                .job(job)
                .candidate(cand1)
                .resume(resume1)
                .coverLetter("Tôi có 5 năm kinh nghiệm backend.")
                .currentStage("APPLIED")
                .status("SUBMITTED")
                .aiMatchScore(new BigDecimal("94.50"))
                .build());

        app2 = applicationRepository.save(ApplicationJpaEntity.builder()
                .job(job)
                .candidate(cand2)
                .resume(resume2)
                .coverLetter("Em mới ra trường có 1 năm kinh nghiệm.")
                .currentStage("REVIEWING")
                .status("SUBMITTED")
                .aiMatchScore(new BigDecimal("72.00"))
                .build());

        // Logins
        recruiterToken = loginUseCase.login(new LoginCommand("hr_screening1@talentbridge.vn", "Password123!")).accessToken();
        otherRecruiterToken = loginUseCase.login(new LoginCommand("hr_other@talentbridge.vn", "Password123!")).accessToken();
    }

    @Test
    @DisplayName("HRPM-45: HR xem danh sách ứng viên nộp vào tin tuyển dụng")
    void shouldReturnApplicantsForCompanyJob() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].candidateFullName", containsInAnyOrder("Nguyen Thi Alice", "Tran Van Bob")));
    }

    @Test
    @DisplayName("HRPM-45: Hỗ trợ route alias /api/v1/jobs/{jobId}/applicants")
    void shouldReturnApplicantsViaAliasRoute() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/{jobId}/applicants", job.getId())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("HRPM-45: HR công ty khác bị từ chối 40301 khi xem ứng viên")
    void shouldDenyAccessWhenRecruiterFromAnotherCompany() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(40301));
    }

    @Test
    @DisplayName("HRPM-46: Lọc ứng viên theo tiêu chí stage và số năm kinh nghiệm")
    void shouldFilterApplicantsByStageAndExperience() throws Exception {
        // Lọc minExperience = 3 -> Chỉ Alice (5 năm)
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .param("minExperience", "3")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].candidateFullName").value("Nguyen Thi Alice"));

        // Lọc stage = REVIEWING -> Chỉ Bob
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .param("stage", "REVIEWING")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].candidateFullName").value("Tran Van Bob"));
    }

    @Test
    @DisplayName("HRPM-46: Tìm kiếm từ khóa theo tên hoặc email ứng viên")
    void shouldSearchApplicantsByKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .param("keyword", "Alice")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].candidateEmail").value("candidate_alice@example.com"));
    }

    @Test
    @DisplayName("HRPM-47: Sắp xếp danh sách ứng viên theo điểm AI match score")
    void shouldSortApplicantsByScore() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants", job.getId())
                        .param("sortBy", "score")
                        .param("sortDirection", "DESC")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data[0].candidateFullName").value("Nguyen Thi Alice"))
                .andExpect(jsonPath("$.data[1].candidateFullName").value("Tran Van Bob"));
    }

    @Test
    @DisplayName("HRPM-48: Cập nhật vòng tuyển dụng và kiểm tra lịch sử audit")
    void shouldUpdateApplicantStageAndCheckAuditHistory() throws Exception {
        UpdateApplicantStatusRequest request = UpdateApplicantStatusRequest.builder()
                .stage("SHORTLISTED")
                .status("ACTIVE")
                .note("Hồ sơ đạt yêu cầu, liên hệ phỏng vấn")
                .build();

        mockMvc.perform(patch("/api/v1/recruiters/jobs/{jobId}/applicants/{appId}/stage", job.getId(), app1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.currentStage").value("SHORTLISTED"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Kiểm tra audit history
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants/{appId}/stages", job.getId(), app1.getId())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].stage").value("SHORTLISTED"))
                .andExpect(jsonPath("$.data[0].note").value("Hồ sơ đạt yêu cầu, liên hệ phỏng vấn"));
    }

    @Test
    @DisplayName("HRPM-49: Đánh giá sao, thêm tag và ghi chú nội bộ cho ứng viên")
    void shouldAddRateAndNoteAndRetrieveNotes() throws Exception {
        RateAndNoteApplicantRequest request = RateAndNoteApplicantRequest.builder()
                .rating(5)
                .tag("High Potential")
                .comment("Phỏng vấn kỹ thuật rất tốt, kiến thức Spring Boot sâu sắc.")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/jobs/{jobId}/applicants/{appId}/notes", job.getId(), app1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.tag").value("High Potential"))
                .andExpect(jsonPath("$.data.comment").value("Phỏng vấn kỹ thuật rất tốt, kiến thức Spring Boot sâu sắc."));

        // Lấy danh sách ghi chú
        mockMvc.perform(get("/api/v1/recruiters/jobs/{jobId}/applicants/{appId}/notes", job.getId(), app1.getId())
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].rating").value(5));
    }
}
