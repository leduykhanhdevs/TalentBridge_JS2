package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApplicationControllerIntegrationTest {

    private static final String URL = "/api/v1/candidates/applications";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private RoleJpaRepository roleRepository;
    @Autowired private CandidateJpaRepository candidateRepository;
    @Autowired private CompanyJpaRepository companyRepository;
    @Autowired private JobJpaRepository jobRepository;
    @Autowired private ResumeJpaRepository resumeRepository;
    @Autowired private ApplicationJpaRepository applicationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private LoginUseCase loginUseCase;

    private String candidateToken;
    private CandidateJpaEntity candidate;
    private JobJpaEntity job;
    private ResumeJpaEntity resume;

    @BeforeEach
    void setUp() {
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(
                        new RoleJpaEntity(null, "ROLE_CANDIDATE", "Candidate Role")));

        UserJpaEntity user = new UserJpaEntity();
        user.setEmail("apply_test@talentbridge.vn");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Ứng viên kiểm thử");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(candidateRole));
        user = userRepository.save(user);

        candidate = new CandidateJpaEntity();
        candidate.setUser(user);
        candidate = candidateRepository.save(candidate);

        CompanyJpaEntity company = new CompanyJpaEntity();
        company.setName("Công ty kiểm thử");
        company.setAddress("Địa chỉ kiểm thử");
        company.setCity("TP. Hồ Chí Minh");
        company.setStatus(CompanyStatus.APPROVED);
        company = companyRepository.save(company);

        job = new JobJpaEntity();
        job.setCompany(company);
        job.setTitle("Java Developer");
        job.setDescription("Tuyển lập trình viên Java");
        job.setStatus(JobStatus.ACTIVE);
        job.setDeadline(LocalDate.now().plusDays(7));
        job = jobRepository.save(job);

        resume = new ResumeJpaEntity();
        resume.setCandidate(candidate);
        resume.setResumeType("UPLOADED");
        resume.setTitle("CV Java");
        resume.setFileName("cv.pdf");
        resume.setFileUrl("resumes/test/cv.pdf");
        resume.setFileType("application/pdf");
        resume.setIsDefault(true);
        resume = resumeRepository.save(resume);

        candidateToken = loginUseCase.login(
                new LoginCommand("apply_test@talentbridge.vn", "Password123!")
        ).accessToken();
    }

    @Test
    void candidateCanSubmitApplicationAndItIsPersisted() throws Exception {
        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.jobId").value(job.getId().intValue()))
                .andExpect(jsonPath("$.data.candidateId").value(candidate.getId().intValue()))
                .andExpect(jsonPath("$.data.currentStage").value("APPLIED"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        Optional<ApplicationJpaEntity> saved = applicationRepository.findAll()
                .stream()
                .filter(application -> application.getJob().getId().equals(job.getId()))
                .findFirst();

        assertTrue(saved.isPresent());
        assertEquals(candidate.getId(), saved.orElseThrow().getCandidate().getId());
        assertEquals(resume.getId(), saved.orElseThrow().getResume().getId());
        assertEquals("Thư ứng tuyển kiểm thử", saved.orElseThrow().getCoverLetter());
    }

    @Test
    void duplicateApplicationReturnsConflictAndKeepsOnlyFirstSubmission() throws Exception {
        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated());

        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40902));

        assertEquals(1, applicationRepository.count());
    }

    @Test
    void candidateCanWithdrawOwnApplicationAndCannotWithdrawItTwice() throws Exception {
        Long applicationId = submitApplication();

        mockMvc.perform(patch(URL + "/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));

        assertEquals("WITHDRAWN", applicationRepository.findById(applicationId).orElseThrow().getStatus());

        mockMvc.perform(patch(URL + "/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40903));
    }

    @Test
    void anotherCandidateCannotWithdrawApplication() throws Exception {
        Long applicationId = submitApplication();
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE").orElseThrow();
        UserJpaEntity otherUser = new UserJpaEntity();
        otherUser.setEmail("other_apply_test@talentbridge.vn");
        otherUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        otherUser.setFullName("Ứng viên khác");
        otherUser.setStatus(UserStatus.ACTIVE);
        otherUser.setRoles(Set.of(candidateRole));
        otherUser = userRepository.save(otherUser);

        CandidateJpaEntity otherCandidate = new CandidateJpaEntity();
        otherCandidate.setUser(otherUser);
        candidateRepository.save(otherCandidate);
        String otherToken = loginUseCase.login(
                new LoginCommand(otherUser.getEmail(), "Password123!")).accessToken();

        mockMvc.perform(patch(URL + "/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        assertEquals("SUBMITTED", applicationRepository.findById(applicationId).orElseThrow().getStatus());
    }

    @Test
    void completedApplicationCannotBeWithdrawn() throws Exception {
        Long applicationId = submitApplication();
        ApplicationJpaEntity application = applicationRepository.findById(applicationId).orElseThrow();
        application.setCurrentStage("HIRED");
        applicationRepository.saveAndFlush(application);

        mockMvc.perform(patch(URL + "/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40904));

        assertEquals("SUBMITTED", applicationRepository.findById(applicationId).orElseThrow().getStatus());
    }

    @Test
    void missingApplicationCannotBeWithdrawn() throws Exception {
        mockMvc.perform(patch(URL + "/999999/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());

        assertEquals(0, applicationRepository.count());
    }

    @Test
    void unauthenticatedCandidateCannotWithdrawApplication() throws Exception {
        Long applicationId = submitApplication();

        mockMvc.perform(patch(URL + "/" + applicationId + "/withdraw"))
                .andExpect(status().isUnauthorized());

        assertEquals("SUBMITTED", applicationRepository.findById(applicationId).orElseThrow().getStatus());
    }

    private Long submitApplication() throws Exception {
        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated());
        return applicationRepository.findAll().getFirst().getId();
    }

    @Test
    void negativeJobIdReturnsBadRequest() throws Exception {
        String request = "{\"jobId\":-1,\"resumeId\":" + resume.getId() + "}";

        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        assertTrue(applicationRepository.findAll().isEmpty());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());

        assertTrue(applicationRepository.findAll().isEmpty());
    }

    @Test
    void recruiterCannotSubmitApplication() throws Exception {
        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(
                        new RoleJpaEntity(null, "ROLE_RECRUITER", "Recruiter Role")));

        UserJpaEntity recruiter = new UserJpaEntity();
        recruiter.setEmail("apply_recruiter_test@talentbridge.vn");
        recruiter.setPasswordHash(passwordEncoder.encode("Password123!"));
        recruiter.setFullName("Nhà tuyển dụng kiểm thử");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.setRoles(Set.of(recruiterRole));
        userRepository.save(recruiter);

        String recruiterToken = loginUseCase.login(
                new LoginCommand("apply_recruiter_test@talentbridge.vn", "Password123!")
        ).accessToken();

        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());

        assertTrue(applicationRepository.findAll().isEmpty());
    }

    private String validRequest() {
        return "{\"jobId\":" + job.getId()
                + ",\"resumeId\":" + resume.getId()
                + ",\"coverLetter\":\"Thư ứng tuyển kiểm thử\"}";
    }
}
