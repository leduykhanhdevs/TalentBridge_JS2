package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.exception.DomainException;

import java.time.LocalDateTime;

public class Application {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;
    private String coverLetter;
    private String currentStage = "APPLIED";
    private String status = "SUBMITTED";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Application() {
    }

    public Application(Long id, Long jobId, Long candidateId, Long resumeId,
                       String coverLetter, String currentStage, String status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.resumeId = resumeId;
        this.coverLetter = coverLetter;
        this.currentStage = currentStage != null ? currentStage : "APPLIED";
        this.status = status != null ? status : "SUBMITTED";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void withdraw() {
        if ("WITHDRAWN".equals(status)) {
            throw new DomainException(40903, "Đơn ứng tuyển đã được rút trước đó");
        }
        if ("HIRED".equals(currentStage) || "REJECTED".equals(currentStage)
                || "ACCEPTED".equals(status) || "DECLINED".equals(status)) {
            throw new DomainException(40904, "Đơn ứng tuyển đã có kết quả, không thể rút");
        }
        status = "WITHDRAWN";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
