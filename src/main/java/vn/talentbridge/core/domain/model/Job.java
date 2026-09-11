package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Job {
    private Long id;
    private Long companyId;
    private String companyName;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private String location;
    private String jobType;
    private String experienceLevel;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private LocalDate deadline;
    private JobStatus status;
    private Long recruiterUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Job() {
        this.status = JobStatus.PENDING;
    }

    public Job(Long id, Long companyId, String companyName, String title, String description,
               String requirements, String benefits, String location, String jobType,
               String experienceLevel, BigDecimal minSalary, BigDecimal maxSalary,
               LocalDate deadline, JobStatus status, Long recruiterUserId,
               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.benefits = benefits;
        this.location = location;
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.deadline = deadline;
        this.status = status != null ? status : JobStatus.PENDING;
        this.recruiterUserId = recruiterUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void approve() { this.status = JobStatus.ACTIVE; }
    public void close() { this.status = JobStatus.CLOSED; }
    public void expire() { this.status = JobStatus.EXPIRED; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public BigDecimal getMinSalary() { return minSalary; }
    public void setMinSalary(BigDecimal minSalary) { this.minSalary = minSalary; }

    public BigDecimal getMaxSalary() { return maxSalary; }
    public void setMaxSalary(BigDecimal maxSalary) { this.maxSalary = maxSalary; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public Long getRecruiterUserId() { return recruiterUserId; }
    public void setRecruiterUserId(Long recruiterUserId) { this.recruiterUserId = recruiterUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}