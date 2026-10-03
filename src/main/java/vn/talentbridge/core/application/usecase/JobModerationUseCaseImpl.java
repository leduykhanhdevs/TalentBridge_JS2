package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.JobStatusHistoryResult;
import vn.talentbridge.core.application.port.in.JobModerationUseCase;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.JobStatusHistoryRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobStatusHistory;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDateTime;
import java.util.List;

public class JobModerationUseCaseImpl implements JobModerationUseCase {
    private final JobRepositoryPort jobRepository;
    private final JobStatusHistoryRepositoryPort historyRepository;

    public JobModerationUseCaseImpl(JobRepositoryPort jobRepository,
                                    JobStatusHistoryRepositoryPort historyRepository) {
        this.jobRepository = jobRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public JobDetailResult changeStatus(Long adminUserId, Long jobId, JobStatus targetStatus, String reason) {
        if (adminUserId == null || jobId == null || targetStatus == null) {
            throw new DomainException(40001, "Thông tin kiểm duyệt tin tuyển dụng không hợp lệ");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        JobStatus currentStatus = job.getStatus();
        boolean approval = currentStatus == JobStatus.PENDING && targetStatus == JobStatus.ACTIVE;
        boolean rejection = currentStatus == JobStatus.PENDING && targetStatus == JobStatus.REJECTED;
        boolean takeDown = currentStatus == JobStatus.ACTIVE && targetStatus == JobStatus.CLOSED;
        if (!approval && !rejection && !takeDown) {
            throw new DomainException(40001, "Chuyển trạng thái kiểm duyệt không hợp lệ");
        }
        String normalizedReason = reason != null ? reason.trim() : "";
        if (!approval && normalizedReason.isBlank()) {
            throw new DomainException(40001, "Vui lòng nhập lý do từ chối hoặc gỡ tin");
        }

        job.setStatus(targetStatus);
        job.setUpdatedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);
        historyRepository.save(new JobStatusHistory(null, jobId, currentStatus, targetStatus,
                normalizedReason.isBlank() ? null : normalizedReason, adminUserId, LocalDateTime.now()));
        return JobDetailResult.from(saved);
    }

    @Override
    public List<JobStatusHistoryResult> getStatusHistory(Long jobId) {
        if (jobId == null || jobRepository.findById(jobId).isEmpty()) {
            throw new ResourceNotFoundException("Tin tuyển dụng", jobId);
        }
        return historyRepository.findByJobId(jobId).stream().map(JobStatusHistoryResult::from).toList();
    }
}
