package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDateTime;

public class JobStatusHistory {
    private Long id;
    private Long jobId;
    private JobStatus fromStatus;
    private JobStatus toStatus;
    private String reason;
    private Long changedByUserId;
    private LocalDateTime changedAt;

    public JobStatusHistory(Long id, Long jobId, JobStatus fromStatus, JobStatus toStatus,
                            String reason, Long changedByUserId, LocalDateTime changedAt) {
        this.id = id;
        this.jobId = jobId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedByUserId = changedByUserId;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public JobStatus getFromStatus() { return fromStatus; }
    public JobStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public Long getChangedByUserId() { return changedByUserId; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
