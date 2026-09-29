package vn.talentbridge.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobApplication {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateAvatarUrl;
    private String candidateTitle;
    private Integer candidateExperienceYears;
    private String candidateCity;
    private String coverLetter;
    private String currentStage;
    private String status;
    private BigDecimal aiMatchScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplication() {
    }

    public JobApplication(Long id, Long jobId, Long candidateId, String candidateFullName,
                          String candidateEmail, String candidatePhone, String candidateAvatarUrl,
                          String candidateTitle, Integer candidateExperienceYears, String candidateCity,
                          String coverLetter, String currentStage, String status,
                          BigDecimal aiMatchScore, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.candidateFullName = candidateFullName;
        this.candidateEmail = candidateEmail;
        this.candidatePhone = candidatePhone;
        this.candidateAvatarUrl = candidateAvatarUrl;
        this.candidateTitle = candidateTitle;
        this.candidateExperienceYears = candidateExperienceYears;
        this.candidateCity = candidateCity;
        this.coverLetter = coverLetter;
        this.currentStage = currentStage;
        this.status = status;
        this.aiMatchScore = aiMatchScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateFullName() {
        return candidateFullName;
    }

    public void setCandidateFullName(String candidateFullName) {
        this.candidateFullName = candidateFullName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }

    public String getCandidateAvatarUrl() {
        return candidateAvatarUrl;
    }

    public void setCandidateAvatarUrl(String candidateAvatarUrl) {
        this.candidateAvatarUrl = candidateAvatarUrl;
    }

    public String getCandidateTitle() {
        return candidateTitle;
    }

    public void setCandidateTitle(String candidateTitle) {
        this.candidateTitle = candidateTitle;
    }

    public Integer getCandidateExperienceYears() {
        return candidateExperienceYears;
    }

    public void setCandidateExperienceYears(Integer candidateExperienceYears) {
        this.candidateExperienceYears = candidateExperienceYears;
    }

    public String getCandidateCity() {
        return candidateCity;
    }

    public void setCandidateCity(String candidateCity) {
        this.candidateCity = candidateCity;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAiMatchScore() {
        return aiMatchScore;
    }

    public void setAiMatchScore(BigDecimal aiMatchScore) {
        this.aiMatchScore = aiMatchScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
