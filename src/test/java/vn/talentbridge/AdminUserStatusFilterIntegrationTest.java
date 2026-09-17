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
import org.springframework.test.web.servlet.MvcResult;
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.*;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Set;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserStatusFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private CandidateJpaRepository candidateJpaRepository;

    @Autowired
    private RecruiterJpaRepository recruiterJpaRepository;

    @Autowired
    private JobJpaRepository jobJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanup();

        RoleJpaEntity adminRole = roleJpaRepository.findByName(RoleName.ROLE_ADMIN.name())
                .orElseGet(() -> roleJpaRepository.save(new RoleJpaEntity(null, RoleName.ROLE_ADMIN.name(), "Admin")));

        RoleJpaEntity candidateRole = roleJpaRepository.findByName(RoleName.ROLE_CANDIDATE.name())
                .orElseGet(() -> roleJpaRepository.save(new RoleJpaEntity(null, RoleName.ROLE_CANDIDATE.name(), "Candidate")));

        // Create Admin
        UserJpaEntity admin = UserJpaEntity.builder()
                .email("admin-filter@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Admin Filter")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();
        userJpaRepository.save(admin);

        // Create Active Candidate
        UserJpaEntity activeUser = UserJpaEntity.builder()
                .email("active-user@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Active User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(candidateRole))
                .build();
        userJpaRepository.save(activeUser);

        // Create Locked Candidate
        UserJpaEntity lockedUser = UserJpaEntity.builder()
                .email("locked-user@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Locked User")
                .status(UserStatus.LOCKED)
                .roles(Set.of(candidateRole))
                .build();
        userJpaRepository.save(lockedUser);

        // Create PENDING and APPROVED companies
        companyJpaRepository.save(CompanyJpaEntity.builder()
                .name("Company Pending")
                .taxCode("TAX001")
                .status(vn.talentbridge.core.domain.vo.CompanyStatus.PENDING)
                .build());

        companyJpaRepository.save(CompanyJpaEntity.builder()
                .name("Company Approved")
                .taxCode("TAX002")
                .status(vn.talentbridge.core.domain.vo.CompanyStatus.APPROVED)
                .build());

        // Login Admin to get Token
        LoginRequest loginRequest = new LoginRequest("admin-filter@talentbridge.vn", "Password123!");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = mvcResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(responseJson).get("data").get("accessToken").asText();
    }

    @AfterEach
    void tearDown() {
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
    @DisplayName("Admin getAllUsers with status=LOCKED filters users and counts accurately")
    void getAllUsers_withStatusLocked_returnsOnlyLockedUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "LOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("locked-user@talentbridge.vn"))
                .andExpect(jsonPath("$.data.content[*].status", everyItem(is("LOCKED"))));
    }

    @Test
    @DisplayName("Admin getAllUsers without status returns all users")
    void getAllUsers_withoutStatus_returnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    @Test
    @DisplayName("Admin getAllCompanies with status=PENDING filters companies and counts accurately")
    void getAllCompanies_withStatusPending_returnsOnlyPendingCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Company Pending"))
                .andExpect(jsonPath("$.data.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Admin getAllCompanies without status returns all companies")
    void getAllCompanies_withoutStatus_returnsAllCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }
}
