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
    private String city;
    private String address;
    private String jobType;
    private String experienceLevel;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Boolean isNegotiable;
    private LocalDate deadline;
    private JobStatus status;
    private Long recruiterUserId;
    private java.util.List<String> skills = new java.util.ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Job() {
        this.status = JobStatus.ACTIVE;
        this.isNegotiable = false;
    }

    public Job(Long id, Long companyId, String companyName, String title, String description,
               String requirements, String benefits, String location, String city, String address,
               String jobType, String experienceLevel, BigDecimal minSalary, BigDecimal maxSalary,
               Boolean isNegotiable, LocalDate deadline, JobStatus status, Long recruiterUserId,
               java.util.List<String> skills, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.benefits = benefits;
        this.location = location;
        this.city = city;
        this.address = address;
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.isNegotiable = isNegotiable != null ? isNegotiable : false;
        this.deadline = deadline;
        this.status = status != null ? status : JobStatus.ACTIVE;
        this.recruiterUserId = recruiterUserId;
        this.skills = skills != null ? skills : new java.util.ArrayList<>();
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

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsNegotiable() { return isNegotiable; }
    public void setIsNegotiable(Boolean isNegotiable) { this.isNegotiable = isNegotiable; }

    public java.util.List<String> getSkills() { return skills; }
    public void setSkills(java.util.List<String> skills) { this.skills = skills; }

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