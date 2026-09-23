package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.ApplicationResult;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.usecase.ApplyJobUseCaseImpl;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Application;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyJobUseCaseImplTest {

    @Mock private ApplicationRepositoryPort applicationRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private JobRepositoryPort jobRepository;
    @Mock private ResumeRepositoryPort resumeRepository;

    private ApplyJobUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplyJobUseCaseImpl(
                applicationRepository,
                candidateRepository,
                jobRepository,
                resumeRepository
        );
    }

    @Test
    void candidateCanApplyWithOwnResume() {
        stubCandidateAndJob(activeJob(LocalDate.now().plusDays(1)));
        when(resumeRepository.findById(30L)).thenReturn(Optional.of(resume(20L)));

        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 23, 10, 0);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            application.setId(40L);
            application.setCreatedAt(createdAt);
            return application;
        });

        ApplicationResult result = useCase.apply(10L, 5L, 30L, "Thư ứng tuyển");

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();

        assertEquals(5L, saved.getJobId());
        assertEquals(20L, saved.getCandidateId());
        assertEquals(30L, saved.getResumeId());
        assertEquals("Thư ứng tuyển", saved.getCoverLetter());
        assertEquals("APPLIED", saved.getCurrentStage());
        assertEquals("SUBMITTED", saved.getStatus());

        assertEquals(40L, result.id());
        assertEquals(20L, result.candidateId());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void inactiveJobIsRejected() {
        Job job = activeJob(LocalDate.now().plusDays(1));
        job.setStatus(JobStatus.PENDING);
        stubCandidateAndJob(job);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(10L, 5L, 30L, null));

        verifyNoInteractions(resumeRepository, applicationRepository);
    }

    @Test
    void expiredDeadlineIsRejected() {
        stubCandidateAndJob(activeJob(LocalDate.now().minusDays(1)));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(10L, 5L, 30L, null));

        verifyNoInteractions(resumeRepository, applicationRepository);
    }

    @Test
    void anotherCandidatesResumeIsRejected() {
        stubCandidateAndJob(activeJob(LocalDate.now().plusDays(1)));
        when(resumeRepository.findById(30L)).thenReturn(Optional.of(resume(21L)));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(10L, 5L, 30L, null));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void missingResumeIsRejected() {
        stubCandidateAndJob(activeJob(LocalDate.now().plusDays(1)));
        when(resumeRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.apply(10L, 5L, 30L, null));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void invalidIdsAreRejectedBeforeAccessingRepositories() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(null, 5L, 30L, null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(10L, -1L, 30L, null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.apply(10L, 5L, null, null));

        verifyNoInteractions(
                candidateRepository, jobRepository,
                resumeRepository, applicationRepository
        );
    }

    @Test
    void missingCandidateProfileIsRejected() {
        when(candidateRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.apply(10L, 5L, 30L, null));

        verifyNoInteractions(jobRepository, resumeRepository, applicationRepository);
    }

    private void stubCandidateAndJob(Job job) {
        Candidate candidate = new Candidate();
        candidate.setId(20L);
        when(candidateRepository.findByUserId(10L))
                .thenReturn(Optional.of(candidate));
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job));
    }

    private Job activeJob(LocalDate deadline) {
        Job job = new Job();
        job.setStatus(JobStatus.ACTIVE);
        job.setDeadline(deadline);
        return job;
    }

    private Resume resume(Long candidateId) {
        Resume resume = new Resume();
        resume.setId(30L);
        resume.setCandidateId(candidateId);
        return resume;
    }
}