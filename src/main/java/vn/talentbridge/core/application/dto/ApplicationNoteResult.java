package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.ApplicationNote;

import java.time.LocalDateTime;

public class ApplicationNoteResult {
    private Long id;
    private Long applicationId;
    private Long recruiterId;
    private String recruiterName;
    private Integer rating;
    private String tag;
    private String comment;
    private LocalDateTime createdAt;

    public ApplicationNoteResult() {
    }

    public static ApplicationNoteResult from(ApplicationNote domain) {
        if (domain == null) return null;
        ApplicationNoteResult result = new ApplicationNoteResult();
        result.setId(domain.getId());
        result.setApplicationId(domain.getApplicationId());
        result.setRecruiterId(domain.getRecruiterId());
        result.setRecruiterName(domain.getRecruiterName());
        result.setRating(domain.getRating());
        result.setTag(domain.getTag());
        result.setComment(domain.getComment());
        result.setCreatedAt(domain.getCreatedAt());
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

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
