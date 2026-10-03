package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.JobStatusHistory;

import java.time.LocalDateTime;

public record JobStatusHistoryResult(
        Long id,
        Long jobId,
        String fromStatus,
        String toStatus,
        String reason,
        Long changedByUserId,
        LocalDateTime changedAt
) {
    public static JobStatusHistoryResult from(JobStatusHistory history) {
        return new JobStatusHistoryResult(history.getId(), history.getJobId(),
                history.getFromStatus() != null ? history.getFromStatus().name() : null,
                history.getToStatus() != null ? history.getToStatus().name() : null,
                history.getReason(), history.getChangedByUserId(), history.getChangedAt());
    }
}
