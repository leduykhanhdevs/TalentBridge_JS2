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
import vn.talentbridge.adapter.in.web.dto.request.UpdateCompanyStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateJobStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateUserStatusRequest;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProviderPort tokenProvider;

    private String adminToken;
    private String candidateToken;
    private UserJpaEntity testUser;
    private CompanyJpaEntity testCompany;
    private JobJpaEntity testJob;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Roles
        RoleJpaEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_ADMIN").build()));
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_CANDIDATE").build()));

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

        // Create Sample Company
        testCompany = CompanyJpaEntity.builder()
                .name("FPT Software")
                .address("Khu Cong Nghe Cao")
                .status(CompanyStatus.PENDING)
                .build();
        testCompany = companyRepository.save(testCompany);

        // Create Sample Job
        testJob = JobJpaEntity.builder()
                .company(testCompany)
                .recruiterUserId(1L)
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

    @Test
    @DisplayName("Admin Dashboard - Lấy số liệu thống kê thành công (HTTP 200)")
    void testGetDashboardStats() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.totalCompanies").value(1))
                .andExpect(jsonPath("$.data.pendingCompanies").value(1))
                .andExpect(jsonPath("$.data.totalJobs").value(1));
    }

    @Test
    @DisplayName("Admin Access Control - Không có quyền ADMIN bị chặn HTTP 403 Forbidden")
    void testForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin Users - Khóa tài khoản người dùng sang BANNED")
    void testUpdateUserStatus() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.BANNED)
                .build();

        mockMvc.perform(patch("/api/v1/admin/users/" + testUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BANNED"));
    }

    @Test
    @DisplayName("Admin Companies - Duyệt doanh nghiệp sang APPROVED")
    void testApproveCompany() throws Exception {
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
}