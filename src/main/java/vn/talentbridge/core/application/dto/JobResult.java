package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Job;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record JobResult(
    Long id,
    Long companyId,
    String companyName,
    String title,
    String location,
    String jobType,
    String experienceLevel,
    BigDecimal minSalary,
    BigDecimal maxSalary,
    LocalDate deadline,
    String status,
    LocalDateTime createdAt
) {
    public static JobResult from(Job job) {
        return new JobResult(
            job.getId(),
            job.getCompanyId(),
            job.getCompanyName(),
            job.getTitle(),
            job.getLocation(),
            job.getJobType(),
            job.getExperienceLevel(),
            job.getMinSalary(),
            job.getMaxSalary(),
            job.getDeadline(),
            job.getStatus() != null ? job.getStatus().name() : null,
            job.getCreatedAt()
        );
    }
}