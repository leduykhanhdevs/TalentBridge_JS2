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
import vn.talentbridge.adapter.in.web.dto.request.RequestCreateCompanyRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateRecruiterProfileRequest;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HieuRecruiterIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProviderPort tokenProvider;

    private UserJpaEntity hrUser1;
    private UserJpaEntity hrUser2;
    private String hrToken1;
    private String hrToken2;
    private CompanyJpaEntity existingCompany;

    @BeforeEach
    void setUp() {
        cleanup();

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));

        // HR 1: Already has company
        hrUser1 = UserJpaEntity.builder()
                .email("hr1@company.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Nguyen Van HR")
                .phoneNumber("0901111111")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser1 = userRepository.save(hrUser1);
        hrToken1 = tokenProvider.generateAccessToken(hrUser1.getId(), hrUser1.getEmail(), "ROLE_RECRUITER");

        existingCompany = CompanyJpaEntity.builder()
                .name("TalentBridge Tech")
                .taxCode("0109998888")
                .website("https://talentbridge.vn")
                .companySize("100-500")
                .address("123 Cong Hoa, Tan Binh")
                .status(CompanyStatus.APPROVED)
                .createdByUserId(hrUser1.getId())
                .build();
        existingCompany = companyRepository.save(existingCompany);

        RecruiterJpaEntity recruiter1 = RecruiterJpaEntity.builder()
                .user(hrUser1)
                .company(existingCompany)
                .position("Lead Recruiter")
                .build();
        recruiterRepository.save(recruiter1);

        // HR 2: Independent HR without company
        hrUser2 = UserJpaEntity.builder()
                .email("hr2@freelance.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Tran Thi HR")
                .phoneNumber("0902222222")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser2 = userRepository.save(hrUser2);
        hrToken2 = tokenProvider.generateAccessToken(hrUser2.getId(), hrUser2.getEmail(), "ROLE_RECRUITER");

        RecruiterJpaEntity recruiter2 = RecruiterJpaEntity.builder()
                .user(hrUser2)
                .company(null)
                .position("Junior HR")
                .build();
        recruiterRepository.save(recruiter2);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==========================================
    // STORY 6: HR PROFILE
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/recruiters/profile - Lấy thông tin hồ sơ HR thành công")
    void testGetRecruiterProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recruiters/profile")
                        .header("Authorization", "Bearer " + hrToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van HR"))
                .andExpect(jsonPath("$.data.position").value("Lead Recruiter"))
                .andExpect(jsonPath("$.data.companyName").value("TalentBridge Tech"));
    }

    @Test
    @DisplayName("PUT /api/v1/recruiters/profile - Cập nhật hồ sơ HR thành công")
    void testUpdateRecruiterProfileSuccess() throws Exception {
        UpdateRecruiterProfileRequest request = new UpdateRecruiterProfileRequest();
        request.setFullName("Nguyen Van Updated");
        request.setPhone("0988776655");
        request.setAvatarUrl("https://example.com/new-avatar.jpg");
        request.setPosition("Senior HR Lead");

        mockMvc.perform(put("/api/v1/recruiters/profile")
                        .header("Authorization", "Bearer " + hrToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Updated"))
                .andExpect(jsonPath("$.data.phone").value("0988776655"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/new-avatar.jpg"))
                .andExpect(jsonPath("$.data.position").value("Senior HR Lead"));
    }

    // ==========================================
    // STORY 7: REQUEST CREATE COMPANY
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/recruiters/companies/request-create - HR chưa có công ty tạo yêu cầu thành công")
    void testRequestCreateCompanySuccess() throws Exception {
        RequestCreateCompanyRequest request = RequestCreateCompanyRequest.builder()
                .name("FPT Software Innovation")
                .taxCode("0107776655")
                .website("https://fpt-innovation.com")
                .companySize("500-1000")
                .address("Khu Cong Nghe Cao, TP Thu Duc")
                .city("Ho Chi Minh")
                .description("Tập đoàn CNTT hàng đầu")
                .logoUrl("https://fpt.com/logo.png")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/companies/request-create")
                        .header("Authorization", "Bearer " + hrToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.name").value("FPT Software Innovation"))
                .andExpect(jsonPath("$.data.city").value("Ho Chi Minh"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/recruiters/companies/request-create - Trùng mã số thuế trả về 409 Conflict")
    void testRequestCreateCompanyDuplicateTaxCode() throws Exception {
        RequestCreateCompanyRequest request = RequestCreateCompanyRequest.builder()
                .name("Another Tech Corp")
                .taxCode("0109998888") // duplicate with existingCompany
                .address("123 Duong 3/2")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/companies/request-create")
                        .header("Authorization", "Bearer " + hrToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40901));
    }

    @Test
    @DisplayName("POST /api/v1/recruiters/companies/request-create - HR đã có công ty trả về 400 Bad Request")
    void testRequestCreateCompanyAlreadyHasCompany() throws Exception {
        RequestCreateCompanyRequest request = RequestCreateCompanyRequest.builder()
                .name("New Brand Corp")
                .taxCode("0108881122")
                .address("456 Le Duan")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/companies/request-create")
                        .header("Authorization", "Bearer " + hrToken1) // hr1 already has company
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40001));
    }
}
