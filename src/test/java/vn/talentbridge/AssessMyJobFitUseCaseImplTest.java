package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.AiMatchingSource;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.ResumeAssessmentStatus;
import vn.talentbridge.core.application.port.out.CandidateJobMatchingPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.CvTextExtractionPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.usecase.AssessMyJobFitUseCaseImpl;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessMyJobFitUseCaseImplTest {
    @Mock private CandidateJobMatchingPort matchingPort;
    @Mock private JobRepositoryPort jobRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private CandidateSkillRepositoryPort candidateSkillRepository;
    @Mock private ResumeRepositoryPort resumeRepository;
    @Mock private FileStoragePort fileStorage;
    @Mock private CvTextExtractionPort cvTextExtraction;

    private AssessMyJobFitUseCaseImpl useCase;
    private Candidate candidate;
    private Job job;
    private CandidateJobMatchResult match;

    @BeforeEach
    void setUp() {
        useCase = new AssessMyJobFitUseCaseImpl(matchingPort, jobRepository, candidateRepository,
                candidateSkillRepository, resumeRepository, fileStorage, cvTextExtraction);
        candidate = new Candidate();
        candidate.setId(2L);
        candidate.setTitle("Backend Developer");
        candidate.setSummary("Java and Spring");
        candidate.setExperienceYears(3);
        candidate.setCity("Hà Nội");
        job = new Job();
        job.setId(1L);
        job.setTitle("Java Engineer");
        job.setStatus(JobStatus.ACTIVE);
        match = new CandidateJobMatchResult(1L, "Java Engineer", 2L, "Ứng viên", 75.0,
                0.7, 0.6, 0.5, List.of(), List.of(), List.of("Java"), List.of("Docker"),
                "Đối chiếu yêu cầu", "Kết quả xác định", AiMatchingSource.DETERMINISTIC);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(candidateRepository.findByUserId(99L)).thenReturn(Optional.of(candidate));
        when(candidateSkillRepository.findByCandidateId(2L)).thenReturn(List.of());
    }

    @Test
    void returnsAdvisoryScoreAndExplainsMissingResumeWithoutCallingExternalProvider() {
        when(resumeRepository.findByCandidateId(2L)).thenReturn(List.of());
        when(resumeRepository.findDefaultByCandidateId(2L)).thenReturn(Optional.empty());
        when(matchingPort.matchLocally(1L, 2L, null)).thenReturn(match);
        when(matchingPort.matchLocally(1L, 2L, null)).thenReturn(match);
        var result = useCase.assess(99L, 1L);

        assertThat(result.match().matchPercentage()).isEqualTo(75.0);
        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.NO_RESUME);
        assertThat(result.missingProfileSections()).contains("CV đã tải lên hoặc tạo", "Kỹ năng");
        assertThat(result.advisoryNotice()).contains("không phải xác suất được tuyển");
        verify(matchingPort).matchLocally(1L, 2L, null);
        verify(matchingPort, never()).match(1L, 2L);
    }

    @Test
    void usesDefaultResumeForLocalTextMatching() {
        Resume defaultResume = new Resume(8L, 2L, null, "UPLOADED", "CV mặc định", "candidate.pdf",
                "resumes/candidate.pdf", "application/pdf", true, null, null, null);
        when(resumeRepository.findByCandidateId(2L)).thenReturn(List.of(defaultResume));
        when(resumeRepository.findDefaultByCandidateId(2L)).thenReturn(Optional.of(defaultResume));
        when(fileStorage.loadFile("resumes/candidate.pdf")).thenReturn(new byte[] { 1, 2, 3 });
        when(cvTextExtraction.extractText(new byte[] { 1, 2, 3 }, "candidate.pdf"))
                .thenReturn("Java Spring Boot");
        when(matchingPort.matchLocally(1L, 2L, "Java Spring Boot")).thenReturn(match);

        var result = useCase.assess(99L, 1L);

        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.EVALUATED);
        verify(matchingPort).matchLocally(1L, 2L, "Java Spring Boot");
    }
}
