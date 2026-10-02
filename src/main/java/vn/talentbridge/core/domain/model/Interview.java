package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class Interview {
    private Long id;
    private Long applicationId;
    private LocalDateTime interviewTime;
    private String locationType;
    private String meetingLinkOrAddress;
    private String notes;
    private String status;
    private LocalDateTime createdAt;

    public Interview() {}

    public Interview(Long id, Long applicationId, LocalDateTime interviewTime,
                     String locationType, String meetingLinkOrAddress,
                     String notes, String status, LocalDateTime createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.interviewTime = interviewTime;
        this.locationType = locationType;
        this.meetingLinkOrAddress = meetingLinkOrAddress;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
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

    public LocalDateTime getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(LocalDateTime interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getMeetingLinkOrAddress() {
        return meetingLinkOrAddress;
    }

    public void setMeetingLinkOrAddress(String meetingLinkOrAddress) {
        this.meetingLinkOrAddress = meetingLinkOrAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
