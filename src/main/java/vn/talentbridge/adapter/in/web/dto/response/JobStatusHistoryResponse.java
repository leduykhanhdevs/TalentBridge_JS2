package vn.talentbridge.adapter.in.web.dto.response;

import vn.talentbridge.core.application.dto.JobStatusHistoryResult;

import java.time.LocalDateTime;

public record JobStatusHistoryResponse(Long id, Long jobId, String fromStatus, String toStatus,
                                       String reason, Long changedByUserId, LocalDateTime changedAt) {
    public static JobStatusHistoryResponse from(JobStatusHistoryResult result) {
        return new JobStatusHistoryResponse(result.id(), result.jobId(), result.fromStatus(), result.toStatus(),
                result.reason(), result.changedByUserId(), result.changedAt());
    }
}
