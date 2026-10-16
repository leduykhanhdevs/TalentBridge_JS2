package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.CandidateSkill;
import vn.talentbridge.core.domain.model.Job;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriVectorRagMatchingServiceTest {
    @Mock private JobRepositoryPort jobRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private CandidateSkillRepositoryPort candidateSkillRepository;

    private TriVectorRagMatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new TriVectorRagMatchingService(
                jobRepository, candidateRepository, candidateSkillRepository);
    }

    @Test
    void returnsThreeBoundedSimilarityDimensionsWithTraceableEvidence() {
        Long jobId = 1L;
        Long candidateId = 7L;
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Backend Engineer");
        job.setExperienceLevel("SENIOR");
        job.setDescription("Build reliable backend systems");
        job.setRequirements("Java Spring Boot REST API SQL");
        job.setSkills(List.of("Java", "Spring Boot", "SQL"));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        candidate.setTitle("Backend Engineer");
        candidate.setSummary("Phát triển Java Spring Boot và REST API");
        candidate.setExperienceYears(5);
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(candidateSkillRepository.findByCandidateId(candidateId)).thenReturn(List.of(
                new CandidateSkill(1L, candidateId, 1, "Java", "ADVANCED", 5, 4.0),
                new CandidateSkill(2L, candidateId, 2, "Spring Boot", "ADVANCED", 5, 3.0),
                new CandidateSkill(3L, candidateId, 3, "SQL", "INTERMEDIATE", 3, 2.0)));

        CandidateJobMatchResult result = matchingService.matchLocally(
                jobId, candidateId, "Built REST APIs using Java Spring Boot and MySQL");

        assertThat(result.matchPercentage()).isBetween(0.0, 100.0);
        assertThat(result.directKeywordScore()).isBetween(0.0, 1.0).isGreaterThan(0.0);
        assertThat(result.contextualSimilarityScore()).isBetween(0.0, 1.0).isGreaterThan(0.0);
        assertThat(result.inferredCapabilityScore()).isBetween(0.0, 1.0).isGreaterThan(0.0);
        assertThat(result.inferredCapabilities()).contains("Phát triển backend và API");
        assertThat(result.verificationQuestions()).anyMatch(question -> question.contains("backend và API"));
        assertThat(result.matchingStrengths()).anyMatch(value -> value.contains("Java"));
        assertThat(result.missingCriticalSkills()).isEmpty();
        assertThat(result.recommendation()).doesNotContain("PHỎNG VẤN", "CHƯA PHÙ HỢP");
        assertThat(result.analysisSummary()).contains("60%", "25%", "15%", "không phải xác suất");
        assertThat(result.source().name()).isEqualTo("DETERMINISTIC");
    }

    @Test
    void unrelatedProfileHasLowSimilarityAndNoFabricatedCapabilities() {
        Long jobId = 2L;
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Chuyên viên Marketing SEO");
        job.setRequirements("Facebook Ads Google Ads copywriting");
        job.setSkills(List.of("Facebook Ads", "SEO", "Copywriting"));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        CandidateJobMatchResult result = matchingService.matchCandidateToJob(
                jobId, null, "Lập trình vi điều khiển C/C++, thiết kế mạch điện tử.");

        assertThat(result.matchPercentage()).isLessThan(40.0);
        assertThat(result.directKeywordScore()).isZero();
        assertThat(result.inferredCapabilityScore()).isZero();
        assertThat(result.inferredCapabilities()).isEmpty();
        assertThat(result.verificationQuestions()).contains("Bạn đã từng sử dụng “Facebook Ads” trong dự án hoặc công việc nào?");
        assertThat(result.missingCriticalSkills()).hasSize(3);
    }

    @Test
    void emptyCandidateEvidenceReceivesZeroInsteadOfAnArtificialBaseline() {
        Long jobId = 3L;
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Senior Java Developer");
        job.setRequirements("Java Spring Boot SQL");
        job.setSkills(List.of("Java", "Spring Boot", "SQL"));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        CandidateJobMatchResult result = matchingService.matchCandidateToJob(jobId, null, "");

        assertThat(result.matchPercentage()).isZero();
        assertThat(result.directKeywordScore()).isZero();
        assertThat(result.contextualSimilarityScore()).isZero();
        assertThat(result.inferredCapabilityScore()).isZero();
        assertThat(result.recommendation()).isEqualTo("ÍT BẰNG CHỨNG TRÙNG KHỚP");
    }

    @Test
    void localCvTextParticipatesInExplicitEvidenceAndRemainsDeterministic() {
        Long jobId = 4L;
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("QA Automation Engineer");
        job.setRequirements("Playwright automated testing");
        job.setSkills(List.of("Playwright", "Testing"));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        CandidateJobMatchResult result = matchingService.matchLocally(
                jobId, null, "Built browser tests with Playwright and automated testing.");

        assertThat(result.directKeywordScore()).isGreaterThan(0.0);
        assertThat(result.inferredCapabilities()).contains("Tự động hóa kiểm thử");
        assertThat(result.source().name()).isEqualTo("DETERMINISTIC");
    }
}
