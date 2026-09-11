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
import vn.talentbridge.adapter.in.web.dto.request.UpdateCompanyStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateJobStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateUserStatusRequest;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

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
    private JobJpaRepository jobRepository;

    @Autowired
    private RecruiterJpaRepository recruiterRepository;

    @Autowired
    private CandidateJpaRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProviderPort tokenProvider;

    private String adminToken;
    private String candidateToken;
    private UserJpaEntity testUser;
    private CompanyJpaEntity testCompany;
    private JobJpaEntity testJob;
    private RecruiterJpaEntity testRecruiter;
    private CandidateJpaEntity testCandidate;

    @BeforeEach
    void setUp() {
        cleanup();

        // Setup Roles
        RoleJpaEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_ADMIN").build()));
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_CANDIDATE").build()));
        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));

        // Create Admin User & Token
        UserJpaEntity adminUser = UserJpaEntity.builder()
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(adminRole)))
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = tokenProvider.generateAccessToken(adminUser.getId(), adminUser.getEmail(), "ROLE_ADMIN");

        // Create Candidate User & Token
        testUser = UserJpaEntity.builder()
                .email("candidate_user@test.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .fullName("Candidate User")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(candidateRole)))
                .build();
        testUser = userRepository.save(testUser);
        candidateToken = tokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail(), "ROLE_CANDIDATE");

        // Create Candidate Profile
        testCandidate = CandidateJpaEntity.builder()
                .user(testUser)
                .title("Fullstack Java Developer")
                .dob(LocalDate.of(2000, 5, 20))
                .gender("MALE")
                .summary("Passionate software engineer")
                .experienceYears(3)
                .expectedSalary(new BigDecimal("25000000"))
                .city("Ho Chi Minh")
                .address("123 Nguyen Trai, Q1")
                .linkedinUrl("https://linkedin.com/in/candidate")
                .githubUrl("https://github.com/candidate")
                .build();
        testCandidate = candidateRepository.save(testCandidate);

        // Create Sample Company
        testCompany = CompanyJpaEntity.builder()
                .name("FPT Software")
                .address("Khu Cong Nghe Cao")
                .status(CompanyStatus.PENDING)
                .build();
        testCompany = companyRepository.save(testCompany);

        // Create Sample Recruiter
        UserJpaEntity hrUser = UserJpaEntity.builder()
                .email("hr@fpt.com")
                .passwordHash(passwordEncoder.encode("hr123"))
                .fullName("Nguyen Van HR")
                .phoneNumber("0987654321")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser = userRepository.save(hrUser);

        testRecruiter = RecruiterJpaEntity.builder()
                .user(hrUser)
                .company(testCompany)
                .position("Senior HR Manager")
                .build();
        testRecruiter = recruiterRepository.save(testRecruiter);

        // Create Sample Job
        testJob = JobJpaEntity.builder()
                .company(testCompany)
                .recruiterUserId(hrUser.getId())
                .title("Senior Java Developer")
                .description("Build microservices")
                .requirements("Java 21, Spring Boot")
                .jobType("FULL_TIME")
                .experienceLevel("SENIOR")
                .minSalary(new BigDecimal("20000000"))
                .maxSalary(new BigDecimal("40000000"))
                .location("Ho Chi Minh")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusDays(30))
                .build();
        testJob = jobRepository.save(testJob);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        candidateRepository.deleteAll();
        recruiterRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Admin Users - Lấy danh sách người dùng thành công (HTTP 200)")
    void testGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    @DisplayName("Admin Users - Khóa tài khoản người dùng sang BANNED")
    void testUpdateUserStatus() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.BANNED)
                .reason("Vi phạm chính sách cộng đồng")
                .build();

        mockMvc.perform(patch("/api/v1/admin/users/" + testUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BANNED"));
    }

    @Test
    @DisplayName("Admin Companies - Lấy danh sách công ty chờ duyệt")
    void testGetAllCompanies_FilterPending() throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("FPT Software"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Admin Companies - Phê duyệt công ty sang APPROVED")
    void testUpdateCompanyStatus() throws Exception {
        UpdateCompanyStatusRequest request = UpdateCompanyStatusRequest.builder()
                .status(CompanyStatus.APPROVED)
                .reason("Giấy phép kinh doanh hợp lệ")
                .build();

        mockMvc.perform(patch("/api/v1/admin/companies/" + testCompany.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Admin Jobs - Đóng tin tuyển dụng vi phạm sang CLOSED")
    void testUpdateJobStatus() throws Exception {
        UpdateJobStatusRequest request = UpdateJobStatusRequest.builder()
                .status(JobStatus.CLOSED)
                .reason("Tin tuyển dụng vi phạm điều khoản")
                .build();

        mockMvc.perform(patch("/api/v1/admin/jobs/" + testJob.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("Admin Recruiters - Lấy danh sách nhà tuyển dụng thành công (HTTP 200)")
    void testGetRecruiters_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/recruiters")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].email").value("hr@fpt.com"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Nguyen Van HR"))
                .andExpect(jsonPath("$.data.content[0].companyName").value("FPT Software"))
                .andExpect(jsonPath("$.data.content[0].position").value("Senior HR Manager"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Admin Recruiters - Tìm kiếm nhà tuyển dụng theo từ khóa")
    void testGetRecruiters_SearchKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/admin/recruiters")
                        .param("keyword", "FPT")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].companyName").value("FPT Software"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/v1/admin/recruiters")
                        .param("keyword", "NonExistent")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("Admin Recruiters - Xem chi tiết nhà tuyển dụng theo ID")
    void testGetRecruiterById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/recruiters/" + testRecruiter.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(testRecruiter.getId()))
                .andExpect(jsonPath("$.data.email").value("hr@fpt.com"))
                .andExpect(jsonPath("$.data.position").value("Senior HR Manager"));
    }

    @Test
    @DisplayName("Admin Recruiters - Không có quyền ADMIN bị chặn HTTP 403 Forbidden")
    void testRecruiters_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/recruiters")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin Candidates - Lấy danh sách ứng viên thành công (HTTP 200)")
    void testGetCandidates_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidates")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].email").value("candidate_user@test.com"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Candidate User"))
                .andExpect(jsonPath("$.data.content[0].title").value("Fullstack Java Developer"))
                .andExpect(jsonPath("$.data.content[0].city").value("Ho Chi Minh"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Admin Candidates - Tìm kiếm ứng viên theo từ khóa và trạng thái")
    void testGetCandidates_SearchAndFilter() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidates")
                        .param("keyword", "Fullstack")
                        .param("status", "ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Fullstack Java Developer"));

        mockMvc.perform(get("/api/v1/admin/candidates")
                        .param("keyword", "NonExistent")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/api/v1/admin/candidates")
                        .param("status", "BANNED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("Admin Candidates - Xem chi tiết hồ sơ ứng viên theo ID")
    void testGetCandidateById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidates/" + testCandidate.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(testCandidate.getId()))
                .andExpect(jsonPath("$.data.email").value("candidate_user@test.com"))
                .andExpect(jsonPath("$.data.fullName").value("Candidate User"))
                .andExpect(jsonPath("$.data.title").value("Fullstack Java Developer"))
                .andExpect(jsonPath("$.data.city").value("Ho Chi Minh"))
                .andExpect(jsonPath("$.data.experienceYears").value(3));
    }

    @Test
    @DisplayName("Admin Candidates - Xem chi tiết ứng viên không tồn tại trả về HTTP 404")
    void testGetCandidateById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidates/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(40401));
    }

    @Test
    @DisplayName("Admin Candidates - Không có quyền ADMIN bị chặn HTTP 403 Forbidden")
    void testCandidates_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidates")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/candidates/" + testCandidate.getId())
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }
}