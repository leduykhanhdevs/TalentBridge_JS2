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
import vn.talentbridge.adapter.in.web.dto.request.CreateJobRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateJobRequest;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.SkillJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobManagementIntegrationTest {

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
    private JobJpaRepository jobRepository;

    @Autowired
    private SkillJpaRepository skillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private UserJpaEntity hrUser;
    private UserJpaEntity candidateUser;
    private String hrToken;
    private String candidateToken;
    private CompanyJpaEntity company;
    private JobJpaEntity sampleJob;

    @BeforeEach
    void setUp() {
        cleanup();

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));

        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_CANDIDATE").build()));

        // Create HR user
        hrUser = UserJpaEntity.builder()
                .email("hr.jobtest@company.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Le Duy Recruiter")
                .phoneNumber("0912345678")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser = userRepository.save(hrUser);

        hrToken = loginUseCase.login(new LoginCommand(hrUser.getEmail(), "secret123")).accessToken();

        // Create Candidate user
        candidateUser = UserJpaEntity.builder()
                .email("candidate.jobtest@candidate.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Tran Van Candidate")
                .phoneNumber("0987654321")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(candidateRole)))
                .build();
        candidateUser = userRepository.save(candidateUser);

        candidateToken = loginUseCase.login(new LoginCommand(candidateUser.getEmail(), "secret123")).accessToken();

        // Create Approved Company
        company = CompanyJpaEntity.builder()
                .name("TalentBridge AI Global")
                .taxCode("0318999888")
                .website("https://talentbridge.ai")
                .companySize("500+")
                .city("Hồ Chí Minh")
                .address("Khu Công Nghệ Cao, TP. Thủ Đức")
                .status(CompanyStatus.APPROVED)
                .createdByUserId(hrUser.getId())
                .build();
        company = companyRepository.save(company);

        // Associate Recruiter with Company
        RecruiterJpaEntity recruiter = RecruiterJpaEntity.builder()
                .user(hrUser)
                .company(company)
                .position("Tech Talent Acquisition Lead")
                .build();
        recruiterRepository.save(recruiter);

        // Create Sample Job
        sampleJob = JobJpaEntity.builder()
                .company(company)
                .recruiterUserId(hrUser.getId())
                .title("Senior Spring Boot Architect")
                .description("Thiết kế và triển khai kiến trúc phần mềm tuyển dụng thông minh")
                .requirements("5+ năm kinh nghiệm Java")
                .benefits("Bảo hiểm quốc tế, thưởng dự án")
                .location("Tại văn phòng")
                .city("Hồ Chí Minh")
                .address("Khu Công Nghệ Cao")
                .jobType("FULL_TIME")
                .experienceLevel("SENIOR")
                .minSalary(new BigDecimal("30000000"))
                .maxSalary(new BigDecimal("50000000"))
                .isNegotiable(false)
                .deadline(LocalDate.now().plusMonths(2))
                .status(JobStatus.ACTIVE)
                .build();
        sampleJob = jobRepository.save(sampleJob);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        jobRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("HRPM-28: POST /api/v1/jobs - HR tạo tin tuyển dụng mới thành công trả về 201 Created")
    void testCreateJobSuccess() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Fullstack Developer (Java & React)")
                .description("Phát triển các module ATS cốt lõi và giao diện người dùng")
                .requirements("Có kinh nghiệm Spring Boot 3 và React 19")
                .benefits("Lương tháng 13, phụ cấp cơm trưa")
                .location("Hybrid")
                .city("Hà Nội")
                .address("Duy Tân, Cầu Giấy")
                .jobType("FULL_TIME")
                .experienceLevel("MIDDLE")
                .minSalary(new BigDecimal("18000000"))
                .maxSalary(new BigDecimal("28000000"))
                .isNegotiable(false)
                .deadline(LocalDate.now().plusDays(30))
                .skills(List.of("Java", "Spring Boot", "React", "TypeScript"))
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.title").value("Fullstack Developer (Java & React)"))
                .andExpect(jsonPath("$.data.companyName").value("TalentBridge AI Global"))
                .andExpect(jsonPath("$.data.city").value("Hà Nội"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.skills", hasItems("Java", "Spring Boot", "React", "TypeScript")));
    }

    @Test
    @DisplayName("HRPM-28: POST /api/v1/jobs - Dữ liệu không hợp lệ trả về 400 Bad Request")
    void testCreateJobValidationFailed() throws Exception {
        // Tiêu đề trống, deadline trong quá khứ, minSalary > maxSalary
        CreateJobRequest request = CreateJobRequest.builder()
                .title("") // Blank
                .description("") // Blank
                .city("") // Blank
                .jobType("") // Blank
                .experienceLevel("") // Blank
                .minSalary(new BigDecimal("40000000"))
                .maxSalary(new BigDecimal("20000000")) // min > max
                .deadline(LocalDate.now().minusDays(5)) // Past
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40002));
    }

    @Test
    @DisplayName("HRPM-28: POST /api/v1/jobs - Chưa đăng nhập trả về 401 Unauthorized")
    void testCreateJobUnauthorized() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Security Test Job")
                .description("Test Description")
                .city("Hà Nội")
                .jobType("FULL_TIME")
                .experienceLevel("JUNIOR")
                .deadline(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HRPM-28: POST /api/v1/jobs - Candidate gọi API tạo tin bị từ chối 403 Forbidden")
    void testCreateJobForbiddenForCandidate() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Candidate Hacking Job")
                .description("Test Description")
                .city("Hà Nội")
                .jobType("FULL_TIME")
                .experienceLevel("JUNIOR")
                .deadline(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HRPM-29: PUT /api/v1/jobs/{id} - HR cập nhật tin tuyển dụng thành công trả về 200 OK")
    void testUpdateJobSuccess() throws Exception {
        UpdateJobRequest updateRequest = UpdateJobRequest.builder()
                .title("Lead Software Architect (Updated)")
                .description("Mô tả công việc đã được nâng cấp")
                .requirements("7+ năm kinh nghiệm")
                .benefits("Gói ESOP hấp dẫn")
                .location("Remote")
                .city("Đà Nẵng")
                .address("Hải Châu")
                .jobType("FULL_TIME")
                .experienceLevel("LEAD")
                .minSalary(new BigDecimal("45000000"))
                .maxSalary(new BigDecimal("70000000"))
                .isNegotiable(false)
                .deadline(LocalDate.now().plusMonths(3))
                .skills(List.of("Microservices", "Kubernetes", "AWS"))
                .build();

        mockMvc.perform(put("/api/v1/jobs/" + sampleJob.getId())
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.title").value("Lead Software Architect (Updated)"))
                .andExpect(jsonPath("$.data.city").value("Đà Nẵng"))
                .andExpect(jsonPath("$.data.location").value("Remote"))
                .andExpect(jsonPath("$.data.skills", hasItems("Microservices", "Kubernetes", "AWS")));
    }

    @Test
    @DisplayName("HRPM-30: PATCH /api/v1/jobs/{id}/close - HR đóng tin tuyển dụng thành công")
    void testCloseJobSuccess() throws Exception {
        mockMvc.perform(patch("/api/v1/jobs/" + sampleJob.getId() + "/close")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("HRPM-31: GET /api/v1/recruiters/my-jobs - HR xem danh sách tin tuyển dụng của mình")
    void testGetRecruiterMyJobs() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/my-jobs")
                        .header("Authorization", "Bearer " + hrToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].title").value("Senior Spring Boot Architect"));
    }

    @Test
    @DisplayName("HRPM-31: GET /api/v1/jobs/my-jobs - Tuyến đường alias cũng trả về danh sách tin của HR")
    void testGetJobsMyJobsAlias() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/my-jobs")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("HRPM-34: GET /api/v1/jobs - Khách vãng lai tìm kiếm việc làm công khai không cần Token")
    void testPublicSearchJobs() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                        .param("keyword", "Spring Boot")
                        .param("location", "Hồ Chí Minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Senior Spring Boot Architect"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("HRPM-34: GET /api/v1/jobs/{id} - Khách xem chi tiết việc làm công khai")
    void testPublicGetJobDetail() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/" + sampleJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(sampleJob.getId()))
                .andExpect(jsonPath("$.data.title").value("Senior Spring Boot Architect"))
                .andExpect(jsonPath("$.data.companyName").value("TalentBridge AI Global"));
    }
}
