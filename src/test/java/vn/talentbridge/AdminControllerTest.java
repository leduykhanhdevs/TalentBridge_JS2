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
import vn.talentbridge.modules.admin.dto.request.UpdateCompanyStatusRequest;
import vn.talentbridge.modules.admin.dto.request.UpdateJobStatusRequest;
import vn.talentbridge.modules.admin.dto.request.UpdateUserStatusRequest;
import vn.talentbridge.modules.company.entity.Company;
import vn.talentbridge.modules.company.enums.CompanyStatus;
import vn.talentbridge.modules.company.repository.CompanyRepository;
import vn.talentbridge.modules.job.entity.Job;
import vn.talentbridge.modules.job.enums.JobStatus;
import vn.talentbridge.modules.job.repository.JobRepository;
import vn.talentbridge.modules.user.entity.Role;
import vn.talentbridge.modules.user.entity.User;
import vn.talentbridge.modules.user.enums.UserStatus;
import vn.talentbridge.modules.user.repository.RoleRepository;
import vn.talentbridge.modules.user.repository.UserRepository;
import vn.talentbridge.security.JwtTokenProvider;
import vn.talentbridge.security.UserPrincipal;

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
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String candidateToken;
    private User testUser;
    private Company testCompany;
    private Job testJob;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CANDIDATE").build()));

        // Create Admin User & Token
        User adminUser = User.builder()
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(adminRole)))
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = tokenProvider.generateAccessToken(new UserPrincipal(adminUser));

        // Create Candidate User & Token
        testUser = User.builder()
                .email("candidate_user@test.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .fullName("Candidate User")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(candidateRole)))
                .build();
        testUser = userRepository.save(testUser);
        candidateToken = tokenProvider.generateAccessToken(new UserPrincipal(testUser));

        // Create Sample Company
        testCompany = Company.builder()
                .name("FPT Software")
                .address("Khu Cong Nghe Cao")
                .city("Ho Chi Minh")
                .status(CompanyStatus.PENDING)
                .build();
        testCompany = companyRepository.save(testCompany);

        // Create Sample Job
        testJob = Job.builder()
                .company(testCompany)
                .recruiterId(1L)
                .categoryId(1)
                .title("Senior Java Developer")
                .description("Build microservices")
                .requirements("Java 21, Spring Boot")
                .jobType("FULL_TIME")
                .experienceLevel("SENIOR")
                .salaryMin(new BigDecimal("20000000"))
                .salaryMax(new BigDecimal("40000000"))
                .city("Ho Chi Minh")
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