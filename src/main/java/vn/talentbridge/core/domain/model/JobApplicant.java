package vn.talentbridge.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobApplicant {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateAvatarUrl;
    private String candidateTitle;
    private Integer candidateExperienceYears;
    private String candidateCity;
    private Long resumeId;
    private String resumeUrl;
    private String resumeFileName;
    private String coverLetter;
    private String currentStage;
    private String status;
    private BigDecimal aiMatchScore;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private Double averageRating;
    private Integer notesCount;

    public JobApplicant() {
    }

    public JobApplicant(Long id, Long jobId, String jobTitle, Long candidateId,
                        String candidateFullName, String candidateEmail, String candidatePhone,
                        String candidateAvatarUrl, String candidateTitle, Integer candidateExperienceYears,
                        String candidateCity, Long resumeId, String resumeUrl, String resumeFileName,
                        String coverLetter, String currentStage, String status, BigDecimal aiMatchScore,
                        LocalDateTime appliedAt, LocalDateTime updatedAt, Double averageRating, Integer notesCount) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.candidateId = candidateId;
        this.candidateFullName = candidateFullName;
        this.candidateEmail = candidateEmail;
        this.candidatePhone = candidatePhone;
        this.candidateAvatarUrl = candidateAvatarUrl;
        this.candidateTitle = candidateTitle;
        this.candidateExperienceYears = candidateExperienceYears;
        this.candidateCity = candidateCity;
        this.resumeId = resumeId;
        this.resumeUrl = resumeUrl;
        this.resumeFileName = resumeFileName;
        this.coverLetter = coverLetter;
        this.currentStage = currentStage;
        this.status = status;
        this.aiMatchScore = aiMatchScore;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
        this.averageRating = averageRating;
        this.notesCount = notesCount;
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
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

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getNotesCount() {
        return notesCount;
    }

    public void setNotesCount(Integer notesCount) {
        this.notesCount = notesCount;
    }
}
