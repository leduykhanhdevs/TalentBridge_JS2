package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.JobStatusHistoryRepositoryPort;
import vn.talentbridge.core.application.usecase.JobModerationUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobStatusHistory;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobModerationUseCaseImplTest {
    @Mock JobRepositoryPort jobRepository;
    @Mock JobStatusHistoryRepositoryPort historyRepository;
    private JobModerationUseCaseImpl useCase;

    @BeforeEach
    void setUp() { useCase = new JobModerationUseCaseImpl(jobRepository, historyRepository); }

    @Test
    void approvesPendingJobAndRecordsActorAndTransition() {
        Job pending = job(JobStatus.PENDING);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(pending));
        when(jobRepository.save(any(Job.class))).thenAnswer(call -> call.getArgument(0));

        var result = useCase.changeStatus(9L, 5L, JobStatus.ACTIVE, null);

        assertThat(result.status()).isEqualTo("ACTIVE");
        ArgumentCaptor<JobStatusHistory> captor = ArgumentCaptor.forClass(JobStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(captor.getValue().getToStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(captor.getValue().getChangedByUserId()).isEqualTo(9L);
        assertThat(captor.getValue().getChangedAt()).isNotNull();
    }

    @Test
    void rejectsPendingJobOnlyWithReason() {
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job(JobStatus.PENDING)));
        assertThatThrownBy(() -> useCase.changeStatus(9L, 5L, JobStatus.REJECTED, "  "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("lý do");
        verify(jobRepository, never()).save(any());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void closesActiveJobWithReasonAndRejectsIllegalTransitions() {
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job(JobStatus.ACTIVE)));
        when(jobRepository.save(any(Job.class))).thenAnswer(call -> call.getArgument(0));

        var result = useCase.changeStatus(9L, 5L, JobStatus.CLOSED, "Nội dung vi phạm");
        assertThat(result.status()).isEqualTo("CLOSED");
        verify(historyRepository).save(argThat(history -> history.getReason().equals("Nội dung vi phạm")));

        when(jobRepository.findById(6L)).thenReturn(Optional.of(job(JobStatus.REJECTED)));
        assertThatThrownBy(() -> useCase.changeStatus(9L, 6L, JobStatus.ACTIVE, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("không hợp lệ");
    }

    private Job job(JobStatus status) {
        Job job = new Job();
        job.setId(5L);
        job.setStatus(status);
        return job;
    }
}
