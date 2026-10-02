package vn.talentbridge;

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
import vn.talentbridge.adapter.out.persistence.entity.*;
import vn.talentbridge.adapter.out.persistence.repository.*;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InterviewSchedulingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationJpaRepository applicationJpaRepository;
    @Autowired private CvTemplateJpaRepository cvTemplateJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private RoleJpaRepository roleJpaRepository;
    @Autowired private CandidateJpaRepository candidateJpaRepository;
    @Autowired private RecruiterJpaRepository recruiterJpaRepository;
    @Autowired private CompanyJpaRepository companyJpaRepository;
    @Autowired private JobJpaRepository jobJpaRepository;
    @Autowired private ResumeJpaRepository resumeJpaRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private LoginUseCase loginUseCase;

    private String candidateToken;
    private String recruiterToken;
    private ApplicationJpaEntity testApplication;

    @BeforeEach
    void setUp() {
        // 1. Ensure CV templates exist
        if (cvTemplateJpaRepository.findByTemplateCode("MODERN_IT_01").isEmpty()) {
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .templateCode("MODERN_IT_01")
                    .name("Modern IT Professional")
                    .description("Bố cục 2 cột")
                    .defaultConfig("MODERN_IT_LAYOUT")
                    .isActive(true)
                    .build());
        }
        if (cvTemplateJpaRepository.findByTemplateCode("CLASSIC_01").isEmpty()) {
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .templateCode("CLASSIC_01")
                    .name("Classic Elegant")
                    .description("Bố cục 1 cột")
                    .defaultConfig("CLASSIC_LAYOUT")
                    .isActive(true)
                    .build());
        }
        if (cvTemplateJpaRepository.findByTemplateCode("MINIMALIST_01").isEmpty()) {
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .templateCode("MINIMALIST_01")
                    .name("Creative Minimalist")
                    .description("Bố cục tối giản")
                    .defaultConfig("MINIMALIST_LAYOUT")
                    .isActive(true)
                    .build());
        }

        // 2. Roles
        RoleJpaEntity candidateRole = roleJpaRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleJpaRepository.save(new RoleJpaEntity(null, "ROLE_CANDIDATE", "Candidate Role")));

        RoleJpaEntity recruiterRole = roleJpaRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleJpaRepository.save(new RoleJpaEntity(null, "ROLE_RECRUITER", "Recruiter Role")));

        // 3. Candidate
        UserJpaEntity candUser = userJpaRepository.findByEmail("test_candidate_interview@talentbridge.vn")
                .orElseGet(() -> userJpaRepository.save(UserJpaEntity.builder()
                        .email("test_candidate_interview@talentbridge.vn")
                        .passwordHash(passwordEncoder.encode("Password123!"))
                        .fullName("Trần Ứng Viên Phỏng Vấn")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(candidateRole))
                        .build()));

        CandidateJpaEntity candidate = candidateJpaRepository.findByUserId(candUser.getId())
                .orElseGet(() -> candidateJpaRepository.save(CandidateJpaEntity.builder()
                        .user(candUser)
                        .title("Senior Backend Engineer")
                        .experienceYears(4)
                        .city("Hà Nội")
                        .build()));

        ResumeJpaEntity resume = resumeJpaRepository.save(ResumeJpaEntity.builder()
                .candidate(candidate)
                .title("CV Senior Backend")
                .fileName("cv_backend.pdf")
                .fileUrl("https://storage.talentbridge.vn/resumes/cv_backend.pdf")
                .isDefault(true)
                .build());

        // 4. Company & Recruiter
        CompanyJpaEntity company = companyJpaRepository.save(CompanyJpaEntity.builder()
                .name("Interview Test Corp")
                .status(CompanyStatus.APPROVED)
                .city("Hà Nội")
                .build());

        UserJpaEntity recUser = userJpaRepository.findByEmail("test_recruiter_interview@talentbridge.vn")
                .orElseGet(() -> userJpaRepository.save(UserJpaEntity.builder()
                        .email("test_recruiter_interview@talentbridge.vn")
                        .passwordHash(passwordEncoder.encode("Password123!"))
                        .fullName("Nguyễn HR Lead")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(recruiterRole))
                        .build()));

        recruiterJpaRepository.findByUserId(recUser.getId())
                .orElseGet(() -> recruiterJpaRepository.save(RecruiterJpaEntity.builder()
                        .user(recUser)
                        .company(company)
                        .position("Lead Recruiter")
                        .build()));

        // 5. Job & Application
        JobJpaEntity job = jobJpaRepository.save(JobJpaEntity.builder()
                .company(company)
                .title("Senior Java Engineer")
                .description("Mô tả tuyển dụng")
                .status(JobStatus.ACTIVE)
                .deadline(LocalDate.now().plusMonths(1))
                .build());

        testApplication = applicationJpaRepository.save(ApplicationJpaEntity.builder()
                .job(job)
                .candidate(candidate)
                .resume(resume)
                .coverLetter("Tôi sẵn sàng phỏng vấn")
                .currentStage("APPLIED")
                .status("SUBMITTED")
                .build());

        // Login tokens
        candidateToken = loginUseCase.login(new LoginCommand("test_candidate_interview@talentbridge.vn", "Password123!")).accessToken();
        recruiterToken = loginUseCase.login(new LoginCommand("test_recruiter_interview@talentbridge.vn", "Password123!")).accessToken();
    }

    @Test
    @DisplayName("Lấy danh sách mẫu CV công khai")
    void shouldGetCvTemplates() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/resumes/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].templateCode").value("MODERN_IT_01"));
    }

    @Test
    @DisplayName("Ứng viên tạo CV mới từ profile theo mẫu")
    void shouldGenerateResumeFromProfile() throws Exception {
        String jsonPayload = """
                {
                    "templateCode": "MODERN_IT_01",
                    "title": "CV Senior Fullstack Developer",
                    "customizationJson": "{\\"primaryColor\\": \\"#2563EB\\"}"
                }
                """;

        mockMvc.perform(post("/api/v1/candidates/resumes/generate")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.title").value("CV Senior Fullstack Developer"))
                .andExpect(jsonPath("$.data.resumeType").value("GENERATED"));
    }

    @Test
    @DisplayName("HR lên lịch phỏng vấn, kiểm tra Google Calendar URL và chuyển stage")
    void shouldScheduleInterviewAndGenerateGoogleCalendar() throws Exception {
        LocalDateTime interviewTime = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        String requestJson = String.format("""
                {
                    "interviewTime": "%s",
                    "locationType": "ONLINE",
                    "meetingLinkOrAddress": "https://meet.google.com/abc-defg-hij",
                    "notes": "Vui lòng chuẩn bị slide giới thiệu và demo project."
                }
                """, interviewTime);

        mockMvc.perform(post("/api/v1/recruiters/applications/" + testApplication.getId() + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.locationType").value("ONLINE"))
                .andExpect(jsonPath("$.data.meetingLinkOrAddress").value("https://meet.google.com/abc-defg-hij"))
                .andExpect(jsonPath("$.data.googleCalendarUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.googleCalendarUrl").value(org.hamcrest.Matchers.containsString("calendar.google.com")));

        // Verify that application stage was automatically transitioned to INTERVIEW
        ApplicationJpaEntity updatedApp = applicationJpaRepository.findById(testApplication.getId()).orElseThrow();
        assertThat(updatedApp.getCurrentStage()).isEqualTo("INTERVIEW");
    }

    @Test
    @DisplayName("Tải file lịch iCalendar .ics công khai thành công")
    void shouldDownloadIcsCalendarFile() throws Exception {
        LocalDateTime interviewTime = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        String requestJson = String.format("""
                {
                    "interviewTime": "%s",
                    "locationType": "ONLINE",
                    "meetingLinkOrAddress": "https://meet.google.com/abc-defg-hij",
                    "notes": "Demo project"
                }
                """, interviewTime);

        String responseBody = mockMvc.perform(post("/api/v1/recruiters/applications/" + testApplication.getId() + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract interview ID from response
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
        long interviewId = rootNode.path("data").path("id").asLong();

        // Test GET /api/v1/interviews/{id}/calendar.ics
        mockMvc.perform(get("/api/v1/interviews/" + interviewId + "/calendar.ics"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Disposition", org.hamcrest.Matchers.containsString("interview-")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("BEGIN:VCALENDAR")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("BEGIN:VEVENT")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("END:VCALENDAR")));
    }
}
