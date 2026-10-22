package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.ApplicantResumeDownload;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.usecase.DownloadApplicantResumeUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.Resume;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadApplicantResumeUseCaseImplTest {
    @Mock private RecruiterRepositoryPort recruiterRepository;
    @Mock private JobRepositoryPort jobRepository;
    @Mock private JobApplicationRepositoryPort applicationRepository;
    @Mock private ResumeRepositoryPort resumeRepository;
    @Mock private FileStoragePort fileStorage;

    private DownloadApplicantResumeUseCaseImpl useCase;
    private Recruiter recruiter;
    private Job job;
    private Resume resume;
    private JobApplicant application;

    @BeforeEach
    void setUp() {
        useCase = new DownloadApplicantResumeUseCaseImpl(recruiterRepository, jobRepository,
                applicationRepository, resumeRepository, fileStorage);
        Company company = new Company();
        company.setId(3L);
        recruiter = new Recruiter();
        recruiter.setCompany(company);
        job = new Job();
        job.setId(4L);
        job.setCompanyId(3L);
        resume = new Resume(5L, 6L, null, "UPLOADED", "CV", "resume.pdf",
                "resumes/resume.pdf", "application/pdf", true, null, null, null);
        application = new JobApplicant();
        application.setJobId(4L);
        application.setCandidateId(6L);
        application.setResumeId(5L);

    }

    @Test
    void recruiterCanLoadOnlyTheResumeAttachedToAnApplicationForTheirJob() {
        givenRecruiterOwnsJob();
        givenApplicationIncludesResume();
        byte[] content = { 37, 80, 68, 70 };
        when(fileStorage.loadFile("resumes/resume.pdf")).thenReturn(content);

        ApplicantResumeDownload result = useCase.downloadForRecruiter(7L, 4L, 5L);

        assertThat(result.fileName()).isEqualTo("resume.pdf");
        assertThat(result.fileType()).isEqualTo("application/pdf");
        assertThat(result.content()).containsExactly(content);
        verify(fileStorage).loadFile("resumes/resume.pdf");
    }

    @Test
    void recruiterCannotLoadAnotherCompanyResume() {
        givenRecruiterOwnsJob();
        job.setCompanyId(99L);

        assertThatThrownBy(() -> useCase.downloadForRecruiter(7L, 4L, 5L))
                .isInstanceOf(DomainException.class)
                .extracting(error -> ((DomainException) error).getCode()).isEqualTo(40301);
    }

    @Test
    void recruiterCannotLoadCandidateResumeThatWasNotSubmittedForThisJob() {
        givenRecruiterOwnsJob();
        givenApplicationIncludesResume();
        application.setResumeId(55L);

        assertThatThrownBy(() -> useCase.downloadForRecruiter(7L, 4L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void givenRecruiterOwnsJob() {
        when(recruiterRepository.findByUserId(7L)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(4L)).thenReturn(Optional.of(job));
    }

    private void givenApplicationIncludesResume() {
        when(resumeRepository.findById(5L)).thenReturn(Optional.of(resume));
        when(applicationRepository.findApplicantByJobIdAndCandidateId(4L, 6L)).thenReturn(Optional.of(application));
    }
}
