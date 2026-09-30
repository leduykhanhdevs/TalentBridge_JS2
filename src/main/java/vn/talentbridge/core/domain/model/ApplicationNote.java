package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class ApplicationNote {
    private Long id;
    private Long applicationId;
    private Long recruiterId;
    private String recruiterName;
    private Integer rating;
    private String tag;
    private String comment;
    private LocalDateTime createdAt;

    public ApplicationNote() {
    }

    public ApplicationNote(Long id, Long applicationId, Long recruiterId, String recruiterName,
                           Integer rating, String tag, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.recruiterId = recruiterId;
        this.recruiterName = recruiterName;
        this.rating = rating;
        this.tag = tag;
        this.comment = comment;
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
