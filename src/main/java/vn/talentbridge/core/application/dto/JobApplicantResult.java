package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.JobApplicant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobApplicantResult {
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

    public JobApplicantResult() {
    }

    public static JobApplicantResult from(JobApplicant domain) {
        if (domain == null) return null;
        JobApplicantResult result = new JobApplicantResult();
        result.setId(domain.getId());
        result.setJobId(domain.getJobId());
        result.setJobTitle(domain.getJobTitle());
        result.setCandidateId(domain.getCandidateId());
        result.setCandidateFullName(domain.getCandidateFullName());
        result.setCandidateEmail(domain.getCandidateEmail());
        result.setCandidatePhone(domain.getCandidatePhone());
        result.setCandidateAvatarUrl(domain.getCandidateAvatarUrl());
        result.setCandidateTitle(domain.getCandidateTitle());
        result.setCandidateExperienceYears(domain.getCandidateExperienceYears());
        result.setCandidateCity(domain.getCandidateCity());
        result.setResumeId(domain.getResumeId());
        result.setResumeUrl(domain.getResumeUrl());
        result.setResumeFileName(domain.getResumeFileName());
        result.setCoverLetter(domain.getCoverLetter());
        result.setCurrentStage(domain.getCurrentStage());
        result.setStatus(domain.getStatus());
        result.setAiMatchScore(domain.getAiMatchScore());
        result.setAppliedAt(domain.getAppliedAt());
        result.setUpdatedAt(domain.getUpdatedAt());
        result.setAverageRating(domain.getAverageRating());
        result.setNotesCount(domain.getNotesCount());
        return result;
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
