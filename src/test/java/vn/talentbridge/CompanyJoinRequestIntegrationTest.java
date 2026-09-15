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
import vn.talentbridge.adapter.in.web.dto.request.ReviewJoinRequest;
import vn.talentbridge.adapter.in.web.dto.request.SubmitJoinCompanyRequest;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJoinRequestJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJoinRequestJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyJoinRequestIntegrationTest {

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
    private CompanyJoinRequestJpaRepository companyJoinRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private UserJpaEntity hrUser1;
    private UserJpaEntity hrUser2;
    private UserJpaEntity hrUser3;
    private String hrToken1;
    private String hrToken2;
    private String hrToken3;
    private CompanyJpaEntity approvedCompany;

    @BeforeEach
    void setUp() {
        cleanup();

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder().name("ROLE_RECRUITER").build()));

        // HR 1: Has approved company
        hrUser1 = UserJpaEntity.builder()
                .email("hr1@company.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Nguyen Van HR")
                .phoneNumber("0901111111")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser1 = userRepository.save(hrUser1);

        hrToken1 = loginUseCase.login(
                new LoginCommand(hrUser1.getEmail(), "secret123")
        ).accessToken();

        approvedCompany = CompanyJpaEntity.builder()
                .name("TalentBridge Tech")
                .taxCode("0109998888")
                .website("https://talentbridge.vn")
                .companySize("100-500")
                .address("123 Cong Hoa, Tan Binh")
                .status(CompanyStatus.APPROVED)
                .createdByUserId(hrUser1.getId())
                .build();
        approvedCompany = companyRepository.save(approvedCompany);

        RecruiterJpaEntity recruiter1 = RecruiterJpaEntity.builder()
                .user(hrUser1)
                .company(approvedCompany)
                .position("Lead Recruiter")
                .build();
        recruiterRepository.save(recruiter1);

        // HR 2: Independent HR without company (applicant)
        hrUser2 = UserJpaEntity.builder()
                .email("hr2@freelance.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Tran Thi HR")
                .phoneNumber("0902222222")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser2 = userRepository.save(hrUser2);

        hrToken2 = loginUseCase.login(
                new LoginCommand(hrUser2.getEmail(), "secret123")
        ).accessToken();

        RecruiterJpaEntity recruiter2 = RecruiterJpaEntity.builder()
                .user(hrUser2)
                .company(null)
                .position("Junior HR")
                .build();
        recruiterRepository.save(recruiter2);

        // HR 3: Belongs to another company
        CompanyJpaEntity otherCompany = CompanyJpaEntity.builder()
                .name("Other Corp")
                .taxCode("0102223333")
                .status(CompanyStatus.APPROVED)
                .build();
        otherCompany = companyRepository.save(otherCompany);

        hrUser3 = UserJpaEntity.builder()
                .email("hr3@other.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .fullName("Le Van HR")
                .phoneNumber("0903333333")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(recruiterRole)))
                .build();
        hrUser3 = userRepository.save(hrUser3);
        
        hrToken3 = loginUseCase.login(
                new LoginCommand(hrUser3.getEmail(), "secret123")
        ).accessToken();

        RecruiterJpaEntity recruiter3 = RecruiterJpaEntity.builder()
                .user(hrUser3)
                .company(otherCompany)
                .position("Manager")
                .build();
        recruiterRepository.save(recruiter3);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        companyJoinRequestRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==========================================
    // STORY 8: SEARCH COMPANIES & SUBMIT JOIN REQUEST
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/recruiters/companies/search - Tìm kiếm công ty đã APPROVED")
    void testSearchApprovedCompanies() throws Exception {
        CompanyJpaEntity pendingCompany = CompanyJpaEntity.builder()
                .name("Pending Startup")
                .taxCode("0991112233")
                .status(CompanyStatus.PENDING)
                .build();
        companyRepository.save(pendingCompany);

        mockMvc.perform(get("/api/v1/recruiters/companies/search")
                        .header("Authorization", "Bearer " + hrToken2)
                        .param("keyword", "TalentBridge")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("TalentBridge Tech"))
                .andExpect(jsonPath("$.data.content[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/recruiters/companies/{id}/join-request - Gửi yêu cầu gia nhập thành công")
    void testSubmitJoinRequestSuccess() throws Exception {
        SubmitJoinCompanyRequest request = SubmitJoinCompanyRequest.builder()
                .position("Senior IT Recruiter")
                .message("Mong muốn cống hiến cho công ty")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/companies/{companyId}/join-request", approvedCompany.getId())
                        .header("Authorization", "Bearer " + hrToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.position").value("Senior IT Recruiter"))
                .andExpect(jsonPath("$.data.companyName").value("TalentBridge Tech"));
    }

    @Test
    @DisplayName("POST /api/v1/recruiters/companies/{id}/join-request - Trùng lặp yêu cầu PENDING trả về 409 Conflict")
    void testSubmitJoinRequestDuplicatePending() throws Exception {
        CompanyJoinRequestJpaEntity existingRequest = CompanyJoinRequestJpaEntity.builder()
                .user(hrUser2)
                .company(approvedCompany)
                .position("Recruiter")
                .status(CompanyJoinRequestStatus.PENDING)
                .build();
        companyJoinRequestRepository.save(existingRequest);

        SubmitJoinCompanyRequest request = SubmitJoinCompanyRequest.builder()
                .position("Another Position")
                .message("Gửi thêm lần nữa")
                .build();

        mockMvc.perform(post("/api/v1/recruiters/companies/{companyId}/join-request", approvedCompany.getId())
                        .header("Authorization", "Bearer " + hrToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40901));
    }

    // ==========================================
    // STORY 9: HR PEER APPROVAL
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/recruiters/companies/my-company/join-requests - Lấy danh sách yêu cầu gia nhập nội bộ")
    void testGetMyCompanyJoinRequests() throws Exception {
        CompanyJoinRequestJpaEntity req = CompanyJoinRequestJpaEntity.builder()
                .user(hrUser2)
                .company(approvedCompany)
                .position("HR Specialist")
                .message("Xin chao")
                .status(CompanyJoinRequestStatus.PENDING)
                .build();
        companyJoinRequestRepository.save(req);

        mockMvc.perform(get("/api/v1/recruiters/companies/my-company/join-requests")
                        .header("Authorization", "Bearer " + hrToken1)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].applicantName").value("Tran Thi HR"))
                .andExpect(jsonPath("$.data.content[0].position").value("HR Specialist"));
    }

    @Test
    @DisplayName("PATCH /api/v1/recruiters/companies/join-requests/{id} - Phê duyệt ACCEPTED gán công ty cho HR thành công")
    void testReviewJoinRequestAcceptSuccess() throws Exception {
        CompanyJoinRequestJpaEntity req = CompanyJoinRequestJpaEntity.builder()
                .user(hrUser2)
                .company(approvedCompany)
                .position("Senior Recruiter")
                .message("Xin chao")
                .status(CompanyJoinRequestStatus.PENDING)
                .build();
        req = companyJoinRequestRepository.save(req);

        ReviewJoinRequest reviewReq = ReviewJoinRequest.builder()
                .status(CompanyJoinRequestStatus.ACCEPTED)
                .reason("Chao mung ban den voi TalentBridge Tech")
                .build();

        mockMvc.perform(patch("/api/v1/recruiters/companies/join-requests/{requestId}", req.getId())
                        .header("Authorization", "Bearer " + hrToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.reason").value("Chao mung ban den voi TalentBridge Tech"));

        RecruiterJpaEntity updatedApplicant = recruiterRepository.findByUserId(hrUser2.getId()).orElse(null);
        assertNotNull(updatedApplicant);
        assertNotNull(updatedApplicant.getCompany());
        assertEquals(approvedCompany.getId(), updatedApplicant.getCompany().getId());
        assertEquals("Senior Recruiter", updatedApplicant.getPosition());
    }

    @Test
    @DisplayName("PATCH /api/v1/recruiters/companies/join-requests/{id} - Từ chối REJECTED lưu lý do thành công")
    void testReviewJoinRequestRejectSuccess() throws Exception {
        CompanyJoinRequestJpaEntity req = CompanyJoinRequestJpaEntity.builder()
                .user(hrUser2)
                .company(approvedCompany)
                .position("HR Trainee")
                .message("Xin chao")
                .status(CompanyJoinRequestStatus.PENDING)
                .build();
        req = companyJoinRequestRepository.save(req);

        ReviewJoinRequest reviewReq = ReviewJoinRequest.builder()
                .status(CompanyJoinRequestStatus.REJECTED)
                .reason("Hien tai cong ty chua co nhu cau tuyen them vi tri nay")
                .build();

        mockMvc.perform(patch("/api/v1/recruiters/companies/join-requests/{requestId}", req.getId())
                        .header("Authorization", "Bearer " + hrToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.reason").value("Hien tai cong ty chua co nhu cau tuyen them vi tri nay"));
    }

    @Test
    @DisplayName("PATCH /api/v1/recruiters/companies/join-requests/{id} - HR công ty khác duyệt trả về 403 Forbidden")
    void testReviewJoinRequestForbiddenDifferentCompany() throws Exception {
        CompanyJoinRequestJpaEntity req = CompanyJoinRequestJpaEntity.builder()
                .user(hrUser2)
                .company(approvedCompany)
                .position("HR Trainee")
                .status(CompanyJoinRequestStatus.PENDING)
                .build();
        req = companyJoinRequestRepository.save(req);

        ReviewJoinRequest reviewReq = ReviewJoinRequest.builder()
                .status(CompanyJoinRequestStatus.ACCEPTED)
                .reason("Duyet")
                .build();

        mockMvc.perform(patch("/api/v1/recruiters/companies/join-requests/{requestId}", req.getId())
                        .header("Authorization", "Bearer " + hrToken3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(40301));
    }
}
