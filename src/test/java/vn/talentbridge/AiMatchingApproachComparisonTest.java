package vn.talentbridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;
import vn.talentbridge.core.application.port.in.TalentBridgeMcpUseCase;
import vn.talentbridge.core.application.port.in.UpdateApplicantStatusUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.MatchApplicantUseCaseImpl;
import vn.talentbridge.core.application.usecase.TalentBridgeMcpUseCaseImpl;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.CandidateSkill;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiMatchingApproachComparisonTest {
    private static final Long RECRUITER_USER_ID = 31L;
    private static final Long JOB_ID = 41L;
    private static final Long CANDIDATE_ID = 51L;
    private static final Long UNRELATED_CANDIDATE_ID = 52L;

    @Test
    @DisplayName("So sánh matching trực tiếp với luồng use case mà công cụ MCP gọi")
    void directMatchingAndMcpUseCaseReturnSameAssessment() {
        JobRepositoryPort jobs = mock(JobRepositoryPort.class);
        CandidateRepositoryPort candidates = mock(CandidateRepositoryPort.class);
        CandidateSkillRepositoryPort skills = mock(CandidateSkillRepositoryPort.class);
        RecruiterRepositoryPort recruiters = mock(RecruiterRepositoryPort.class);
        JobApplicationRepositoryPort applications = mock(JobApplicationRepositoryPort.class);

        Job job = new Job();
        job.setId(JOB_ID);
        job.setCompanyId(71L);
        job.setTitle("Backend Developer Java Spring Boot");
        job.setRequirements("Java, Spring Boot, MySQL, REST API, Docker");
        job.setDescription("Xây dựng dịch vụ backend và API tuyển dụng an toàn.");
        job.setExperienceLevel("MID");
        job.setSkills(List.of("Java", "Docker"));

        Candidate relevantCandidate = candidate(CANDIDATE_ID, "Java Backend Developer",
                "Bốn năm xây dựng backend bằng Java Spring Boot, MySQL, REST API và Docker.");
        Candidate unrelatedCandidate = candidate(UNRELATED_CANDIDATE_ID, "Marketing specialist",
                "Marketing, SEO, Google Ads, Photoshop và nội dung truyền thông.");

        Company company = new Company();
        company.setId(71L);
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(company);

        when(jobs.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(candidates.findById(CANDIDATE_ID)).thenReturn(Optional.of(relevantCandidate));
        when(candidates.findById(UNRELATED_CANDIDATE_ID)).thenReturn(Optional.of(unrelatedCandidate));
        when(skills.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of(
                new CandidateSkill(1L, CANDIDATE_ID, 1, "Java", "ADVANCED", 5, 4.0),
                new CandidateSkill(2L, CANDIDATE_ID, 2, "Spring Boot", "ADVANCED", 5, 4.0)));
        when(skills.findByCandidateId(UNRELATED_CANDIDATE_ID)).thenReturn(List.of());
        when(recruiters.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(applications.findApplicantByJobIdAndCandidateId(JOB_ID, CANDIDATE_ID))
                .thenReturn(Optional.of(new JobApplicant()));
        when(applications.findApplicantByJobIdAndCandidateId(JOB_ID, UNRELATED_CANDIDATE_ID))
                .thenReturn(Optional.of(new JobApplicant()));

        TriVectorRagMatchingService matching = new TriVectorRagMatchingService(jobs, candidates, skills);
        MatchApplicantUseCase matchApplicant = new MatchApplicantUseCaseImpl(recruiters, jobs, applications, matching);
        TalentBridgeMcpUseCase mcpUseCase = new TalentBridgeMcpUseCaseImpl(
                mock(JobUseCase.class), matchApplicant, mock(UpdateApplicantStatusUseCase.class),
                recruiters, jobs, applications, candidates, skills);

        var direct = matching.match(JOB_ID, CANDIDATE_ID);
        var viaMcpUseCase = mcpUseCase.evaluateCandidate(RECRUITER_USER_ID, JOB_ID, CANDIDATE_ID);
        var unrelatedDirect = matching.match(JOB_ID, UNRELATED_CANDIDATE_ID);
        var unrelatedViaMcpUseCase = mcpUseCase.evaluateCandidate(
                RECRUITER_USER_ID, JOB_ID, UNRELATED_CANDIDATE_ID);

        assertThat(viaMcpUseCase).isEqualTo(direct);
        assertThat(unrelatedViaMcpUseCase).isEqualTo(unrelatedDirect);
        assertThat(direct.matchPercentage()).isBetween(0.0, 100.0).isGreaterThan(1.0);
        assertThat(direct.matchPercentage()).isGreaterThan(unrelatedDirect.matchPercentage());

        for (int i = 0; i < 200; i++) {
            matching.match(JOB_ID, CANDIDATE_ID);
            mcpUseCase.evaluateCandidate(RECRUITER_USER_ID, JOB_ID, CANDIDATE_ID);
        }
        long directNanos = 0L;
        long mcpNanos = 0L;
        int samples = 500;
        for (int i = 0; i < samples; i++) {
            long started = System.nanoTime();
            matching.match(JOB_ID, CANDIDATE_ID);
            directNanos += System.nanoTime() - started;
            started = System.nanoTime();
            mcpUseCase.evaluateCandidate(RECRUITER_USER_ID, JOB_ID, CANDIDATE_ID);
            mcpNanos += System.nanoTime() - started;
        }

        double directAverageMicros = directNanos / (double) samples / 1_000.0;
        double mcpAverageMicros = mcpNanos / (double) samples / 1_000.0;
        System.out.printf("AI_MATCHING_COMPARISON fixture=synthetic warmup=200 samples=%d direct_avg_us=%.2f mcp_usecase_avg_us=%.2f parity=100%% relevant_score=%.1f unrelated_score=%.1f synthetic_pair_order=passed%n",
                samples, directAverageMicros, mcpAverageMicros,
                direct.matchPercentage(), unrelatedDirect.matchPercentage());
    }

    private static Candidate candidate(Long id, String title, String summary) {
        User user = mock(User.class);
        when(user.getFullName()).thenReturn("Ứng viên thử nghiệm " + id);
        Candidate candidate = new Candidate();
        candidate.setId(id);
        candidate.setUser(user);
        candidate.setTitle(title);
        candidate.setSummary(summary);
        candidate.setExperienceYears(4);
        return candidate;
    }
}
