package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.JobStatusHistoryResult;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;

public interface JobModerationUseCase {
    JobDetailResult changeStatus(Long adminUserId, Long jobId, JobStatus targetStatus, String reason);
    List<JobStatusHistoryResult> getStatusHistory(Long jobId);
}
