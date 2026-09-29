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
import vn.talentbridge.adapter.out.persistence.entity.*;
import vn.talentbridge.adapter.out.persistence.repository.*;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecruiterJobApplicantsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RoleJpaRepository roleRepository;

    @Autowired
    private CompanyJpaRepository companyRepository;

    @Autowired
    private RecruiterJpaRepository recruiterRepository;

    @Autowired
    private CandidateJpaRepository candidateRepository;

    @Autowired
    private JobJpaRepository jobRepository;

    @Autowired
    private ApplicationJpaRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private UserJpaEntity hrUserCompanyA;
    private UserJpaEntity hrUserCompanyB;
    private UserJpaEntity candidateUser;
    private String hrTokenCompanyA;
    private String hrTokenCompanyB;
    private String candidateToken;

    private CompanyJpaEntity companyA;
    private CompanyJpaEntity companyB;
    private JobJpaEntity jobCompanyAWithApplicants;
    private JobJpaEntity jobCompanyAEmpty;
    private JobJpaEntity jobCompanyB;

    @BeforeEach
    void setUp() {
        cleanup();

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_CANDIDATE").build()));

        // Company A
        companyA = companyRepository.save(CompanyJpaEntity.builder()
                .name("Công ty A - FPT Software")
                .status(CompanyStatus.APPROVED)
                .build());

        // Company B
        companyB = companyRepository.save(CompanyJpaEntity.builder()
                .name("Công ty B - Viettel")
                .status(CompanyStatus.APPROVED)
                .build());

        // HR Company A
        hrUserCompanyA = userRepository.save(UserJpaEntity.builder()
                .email("hr.a@fpt.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("HR Nguyễn Văn A")
                .phoneNumber("0911111111")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build());

        recruiterRepository.save(RecruiterJpaEntity.builder()
                .user(hrUserCompanyA)
                .company(companyA)
                .position("Senior Talent Acquisition")
                .build());

        hrTokenCompanyA = loginUseCase.login(new LoginCommand(hrUserCompanyA.getEmail(), "Password123!")).accessToken();

        // HR Company B
        hrUserCompanyB = userRepository.save(UserJpaEntity.builder()
                .email("hr.b@viettel.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("HR Lê Thị B")
                .phoneNumber("0922222222")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build());

        recruiterRepository.save(RecruiterJpaEntity.builder()
                .user(hrUserCompanyB)
                .company(companyB)
                .position("HR Lead")
                .build());

        hrTokenCompanyB = loginUseCase.login(new LoginCommand(hrUserCompanyB.getEmail(), "Password123!")).accessToken();

        // Candidate User
        candidateUser = userRepository.save(UserJpaEntity.builder()
                .email("candidate.test@gmail.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Ứng Viên Trần Minh Anh")
                .phoneNumber("0988776655")
                .avatarUrl("https://example.com/avatar.png")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(candidateRole)))
                .build());

        CandidateJpaEntity candidate = candidateRepository.save(CandidateJpaEntity.builder()
                .user(candidateUser)
                .title("Fullstack Developer")
                .experienceYears(3)
                .city("TP. Hồ Chí Minh")
                .build());

        candidateToken = loginUseCase.login(new LoginCommand(candidateUser.getEmail(), "Password123!")).accessToken();

        // Jobs for Company A
        jobCompanyAWithApplicants = jobRepository.save(JobJpaEntity.builder()
                .company(companyA)
                .title("Java Software Engineer")
                .description("Mô tả công việc Java")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusMonths(1))
                .build());

        jobCompanyAEmpty = jobRepository.save(JobJpaEntity.builder()
                .company(companyA)
                .title("DevOps Engineer")
                .description("Mô tả công việc DevOps")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusMonths(1))
                .build());

        // Job for Company B
        jobCompanyB = jobRepository.save(JobJpaEntity.builder()
                .company(companyB)
                .title("Product Manager")
                .description("Mô tả công việc PM")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusMonths(1))
                .build());

        // Application to JobCompanyAWithApplicants
        applicationRepository.save(ApplicationJpaEntity.builder()
                .job(jobCompanyAWithApplicants)
                .candidate(candidate)
                .coverLetter("Tôi rất mong muốn đóng góp cho dự án FPT")
                .currentStage("APPLIED")
                .status("SUBMITTED")
                .aiMatchScore(new BigDecimal("91.50"))
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        recruiterRepository.deleteAll();
        candidateRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{jobId}/applicants - HR xem applicants của Job thuộc công ty mình (200 OK)")
    void shouldReturnApplicants_whenHRViewsOwnCompanyJob() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/" + jobCompanyAWithApplicants.getId() + "/applicants")
                        .header("Authorization", "Bearer " + hrTokenCompanyA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].jobId").value(jobCompanyAWithApplicants.getId()))
                .andExpect(jsonPath("$.data[0].fullName").value("Ứng Viên Trần Minh Anh"))
                .andExpect(jsonPath("$.data[0].email").value("candidate.test@gmail.com"))
                .andExpect(jsonPath("$.data[0].phone").value("0988776655"))
                .andExpect(jsonPath("$.data[0].avatar").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.data[0].title").value("Fullstack Developer"))
                .andExpect(jsonPath("$.data[0].yearsOfExperience").value(3))
                .andExpect(jsonPath("$.data[0].city").value("TP. Hồ Chí Minh"))
                .andExpect(jsonPath("$.data[0].coverLetter").value("Tôi rất mong muốn đóng góp cho dự án FPT"))
                .andExpect(jsonPath("$.data[0].currentStage").value("APPLIED"))
                .andExpect(jsonPath("$.data[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{jobId}/applicants - Job không có applicants (200 OK + [])")
    void shouldReturnEmptyList_whenJobHasNoApplicants() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/" + jobCompanyAEmpty.getId() + "/applicants")
                        .header("Authorization", "Bearer " + hrTokenCompanyA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{jobId}/applicants - Job không tồn tại (404 Not Found)")
    void shouldReturn404_whenJobDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/999999/applicants")
                        .header("Authorization", "Bearer " + hrTokenCompanyA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(40401));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{jobId}/applicants - HR xem Job thuộc công ty khác (403 Forbidden)")
    void shouldReturn403_whenHRViewsOtherCompanyJob() throws Exception {
        // HR Company A tries to view applicants of Job from Company B
        mockMvc.perform(get("/api/v1/jobs/" + jobCompanyB.getId() + "/applicants")
                        .header("Authorization", "Bearer " + hrTokenCompanyA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(40301));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{jobId}/applicants - Không có Token hoặc role Candidate bị chặn (401/403)")
    void shouldDenyAccess_whenUnauthenticatedOrWrongRole() throws Exception {
        // 1. No token -> 401
        mockMvc.perform(get("/api/v1/jobs/" + jobCompanyAWithApplicants.getId() + "/applicants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // 2. Candidate role -> 403
        mockMvc.perform(get("/api/v1/jobs/" + jobCompanyAWithApplicants.getId() + "/applicants")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/jobs/my-company - HR lấy danh sách tin tuyển dụng công ty mình (200 OK)")
    void shouldReturnCompanyJobs_forRecruiter() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/my-company")
                        .header("Authorization", "Bearer " + hrTokenCompanyA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }
}
