package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class ApplicationStage {
    private Long id;
    private Long applicationId;
    private String stage;
    private String note;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;

    public ApplicationStage() {
    }

    public ApplicationStage(Long id, Long applicationId, String stage, String note,
                            Long changedByUserId, String changedByUserName, LocalDateTime changedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.stage = stage;
        this.note = note;
        this.changedByUserId = changedByUserId;
        this.changedByUserName = changedByUserName;
        this.changedAt = changedAt;
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
