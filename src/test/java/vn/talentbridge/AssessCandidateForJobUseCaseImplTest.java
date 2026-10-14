package vn.talentbridge;

import org.junit.jupiter.api.Test;
import vn.talentbridge.core.application.dto.AiMatchingSource;
import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.ResumeAssessmentStatus;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.CvTextExtractionPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.usecase.AssessCandidateForJobUseCaseImpl;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Resume;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AssessCandidateForJobUseCaseImplTest {
    private static final Long RECRUITER_ID = 14L;
    private static final Long JOB_ID = 42L;
    private static final Long CANDIDATE_ID = 73L;

    private final MatchApplicantUseCase matchingUseCase = mock(MatchApplicantUseCase.class);
    private final CandidateRepositoryPort candidateRepository = mock(CandidateRepositoryPort.class);
    private final CandidateSkillRepositoryPort candidateSkillRepository = mock(CandidateSkillRepositoryPort.class);
    private final ResumeRepositoryPort resumeRepository = mock(ResumeRepositoryPort.class);
    private final FileStoragePort fileStorage = mock(FileStoragePort.class);
    private final CvTextExtractionPort cvTextExtraction = mock(CvTextExtractionPort.class);

    private final AssessCandidateForJobUseCaseImpl useCase = new AssessCandidateForJobUseCaseImpl(
            matchingUseCase, candidateRepository, candidateSkillRepository,
            resumeRepository, fileStorage, cvTextExtraction);

    @Test
    void extractsTheDefaultResumeOnlyAfterAuthorizationAndMatchesLocally() {
        stubCandidate();
        Resume resume = resume();
        when(resumeRepository.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of(resume));
        when(fileStorage.loadFile(resume.getFileUrl())).thenReturn(new byte[]{1, 2, 3});
        when(cvTextExtraction.extractText(new byte[]{1, 2, 3}, resume.getFileName()))
                .thenReturn("Java Spring Boot REST API");
        when(matchingUseCase.matchApplicantLocally(RECRUITER_ID, JOB_ID, CANDIDATE_ID,
                "Java Spring Boot REST API")).thenReturn(matchResult());

        CandidateJobAssessmentResult result = useCase.assess(RECRUITER_ID, JOB_ID, CANDIDATE_ID);

        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.EVALUATED);
        assertThat(result.match()).isEqualTo(matchResult());
        var order = inOrder(matchingUseCase, fileStorage);
        order.verify(matchingUseCase).assertCanAssessApplicant(RECRUITER_ID, JOB_ID, CANDIDATE_ID);
        order.verify(fileStorage).loadFile(resume.getFileUrl());
        verify(matchingUseCase).matchApplicantLocally(RECRUITER_ID, JOB_ID, CANDIDATE_ID,
                "Java Spring Boot REST API");
    }

    @Test
    void reportsMissingResumeAndFallsBackToProfileWithoutReadingFiles() {
        stubCandidate();
        when(resumeRepository.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of());
        when(matchingUseCase.matchApplicantLocally(RECRUITER_ID, JOB_ID, CANDIDATE_ID, null))
                .thenReturn(matchResult());

        CandidateJobAssessmentResult result = useCase.assess(RECRUITER_ID, JOB_ID, CANDIDATE_ID);

        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.NO_RESUME);
        assertThat(result.missingProfileSections()).contains("CV đã tải lên hoặc tạo");
        verifyNoInteractions(fileStorage, cvTextExtraction);
    }

    @Test
    void reportsUnavailableStoredContentAndFallsBackToProfile() {
        stubCandidate();
        Resume resume = resume();
        when(resumeRepository.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of(resume));
        when(fileStorage.loadFile(resume.getFileUrl())).thenThrow(new IllegalArgumentException("not found"));
        when(matchingUseCase.matchApplicantLocally(RECRUITER_ID, JOB_ID, CANDIDATE_ID, null))
                .thenReturn(matchResult());

        CandidateJobAssessmentResult result = useCase.assess(RECRUITER_ID, JOB_ID, CANDIDATE_ID);

        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.CONTENT_UNAVAILABLE);
        verifyNoInteractions(cvTextExtraction);
    }

    @Test
    void reportsUnreadableCvTextAndFallsBackToProfile() {
        stubCandidate();
        Resume resume = resume();
        when(resumeRepository.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of(resume));
        when(fileStorage.loadFile(resume.getFileUrl())).thenReturn(new byte[]{1});
        when(cvTextExtraction.extractText(new byte[]{1}, resume.getFileName())).thenReturn("  \n");
        when(matchingUseCase.matchApplicantLocally(RECRUITER_ID, JOB_ID, CANDIDATE_ID, null))
                .thenReturn(matchResult());

        CandidateJobAssessmentResult result = useCase.assess(RECRUITER_ID, JOB_ID, CANDIDATE_ID);

        assertThat(result.resumeAssessmentStatus()).isEqualTo(ResumeAssessmentStatus.NO_READABLE_TEXT);
    }

    @Test
    void rejectsUnauthorizedAssessmentBeforeLoadingCandidateOrResume() {
        doThrow(new IllegalStateException("forbidden"))
                .when(matchingUseCase).assertCanAssessApplicant(RECRUITER_ID, JOB_ID, CANDIDATE_ID);

        assertThatThrownBy(() -> useCase.assess(RECRUITER_ID, JOB_ID, CANDIDATE_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forbidden");

        verifyNoInteractions(candidateRepository, candidateSkillRepository, resumeRepository,
                fileStorage, cvTextExtraction);
        verify(matchingUseCase, never()).matchApplicantLocally(any(), any(), any(), any());
    }

    private void stubCandidate() {
        Candidate candidate = new Candidate();
        candidate.setId(CANDIDATE_ID);
        candidate.setTitle("Backend developer");
        candidate.setSummary("Builds Java services");
        candidate.setCity("Hồ Chí Minh");
        candidate.setExperienceYears(3);
        when(candidateRepository.findById(CANDIDATE_ID)).thenReturn(Optional.of(candidate));
        when(candidateSkillRepository.findByCandidateId(CANDIDATE_ID)).thenReturn(List.of());
    }

    private Resume resume() {
        return new Resume(1L, CANDIDATE_ID, null, "UPLOADED", "CV chính", "resume.pdf",
                "resumes/73/resume.pdf", "application/pdf", true, null, null, null);
    }

    private CandidateJobMatchResult matchResult() {
        return new CandidateJobMatchResult(JOB_ID, "Backend Developer", CANDIDATE_ID, "Candidate",
                72.0, 0.75, 0.60, List.of("backend"), List.of("Java"), List.of(),
                "CẦN ĐÁNH GIÁ THÊM", "Local matching", AiMatchingSource.DETERMINISTIC);
    }
}
