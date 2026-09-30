package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.ApplicationStage;

import java.time.LocalDateTime;

public class ApplicationStageResult {
    private Long id;
    private Long applicationId;
    private String stage;
    private String note;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;

    public ApplicationStageResult() {
    }

    public static ApplicationStageResult from(ApplicationStage domain) {
        if (domain == null) return null;
        ApplicationStageResult result = new ApplicationStageResult();
        result.setId(domain.getId());
        result.setApplicationId(domain.getApplicationId());
        result.setStage(domain.getStage());
        result.setNote(domain.getNote());
        result.setChangedByUserId(domain.getChangedByUserId());
        result.setChangedByUserName(domain.getChangedByUserName());
        result.setChangedAt(domain.getChangedAt());
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByUserName() {
        return changedByUserName;
    }

    public void setChangedByUserName(String changedByUserName) {
        this.changedByUserName = changedByUserName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
