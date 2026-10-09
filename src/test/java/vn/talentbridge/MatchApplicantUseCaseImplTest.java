package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.talentbridge.core.application.dto.AiMatchingSource;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.port.out.CandidateJobMatchingPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.MatchApplicantUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MatchApplicantUseCaseImplTest {

    private final RecruiterRepositoryPort recruiterRepository = mock(RecruiterRepositoryPort.class);
    private final JobRepositoryPort jobRepository = mock(JobRepositoryPort.class);
    private final JobApplicationRepositoryPort applicationRepository = mock(JobApplicationRepositoryPort.class);
    private final CandidateJobMatchingPort matchingPort = mock(CandidateJobMatchingPort.class);
    private final MatchApplicantUseCaseImpl useCase = new MatchApplicantUseCaseImpl(
            recruiterRepository, jobRepository, applicationRepository, matchingPort);

    private final Long recruiterUserId = 14L;
    private final Long jobId = 42L;
    private final Long candidateId = 73L;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(9L);
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(company);
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));

        Job job = new Job();
        job.setId(jobId);
        job.setCompanyId(company.getId());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        JobApplicant applicant = new JobApplicant();
        applicant.setJobId(jobId);
        applicant.setCandidateId(candidateId);
        when(applicationRepository.findApplicantByJobIdAndCandidateId(jobId, candidateId))
                .thenReturn(Optional.of(applicant));
    }

    @Test
    void matchesOnlyAnApplicantOfAJobOwnedByRecruitersCompany() {
        CandidateJobMatchResult expected = new CandidateJobMatchResult(
                jobId, "Backend Developer", candidateId, "Candidate", 86.0, 0.82, 0.90,
                List.of("REST API"), List.of("Java"), List.of(), "CẦN ĐÁNH GIÁ THÊM", "Analysis",
                AiMatchingSource.DETERMINISTIC);
        when(matchingPort.match(jobId, candidateId)).thenReturn(expected);

        CandidateJobMatchResult actual = useCase.matchApplicant(recruiterUserId, jobId, candidateId);

        assertThat(actual).isEqualTo(expected);
        verify(matchingPort).match(jobId, candidateId);
    }

    @Test
    void rejectsJobsOwnedByAnotherCompanyBeforeCallingMatcher() {
        Job foreignJob = new Job();
        foreignJob.setId(jobId);
        foreignJob.setCompanyId(999L);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(foreignJob));

        assertThatThrownBy(() -> useCase.matchApplicant(recruiterUserId, jobId, candidateId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("không có quyền");

        verifyNoInteractions(applicationRepository, matchingPort);
    }

    @Test
    void rejectsCandidateWhoHasNotAppliedToTheJob() {
        when(applicationRepository.findApplicantByJobIdAndCandidateId(jobId, candidateId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.matchApplicant(recruiterUserId, jobId, candidateId))
                .hasMessageContaining("Đơn ứng tuyển");

        verifyNoInteractions(matchingPort);
    }
}
