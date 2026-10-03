package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.JobStatusHistoryResult;
import vn.talentbridge.core.application.port.in.JobModerationUseCase;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;

public class TransactionalJobModerationUseCase implements JobModerationUseCase {
    private final JobModerationUseCase delegate;

    public TransactionalJobModerationUseCase(JobModerationUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobDetailResult changeStatus(Long adminUserId, Long jobId, JobStatus targetStatus, String reason) {
        return delegate.changeStatus(adminUserId, jobId, targetStatus, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobStatusHistoryResult> getStatusHistory(Long jobId) {
        return delegate.getStatusHistory(jobId);
    }
}
