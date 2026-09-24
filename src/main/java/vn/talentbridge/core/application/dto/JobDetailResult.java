package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Job;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobDetailResult(
        Long id,
        Long companyId,
        String companyName,
        String title,
        String description,
        String requirements,
        String benefits,
        String location,
        String city,
        String address,
        String jobType,
        String experienceLevel,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        Boolean isNegotiable,
        LocalDate deadline,
        String status,
        Long recruiterUserId,
        List<String> skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobDetailResult from(Job job) {
        return new JobDetailResult(
                job.getId(),
                job.getCompanyId(),
                job.getCompanyName(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getBenefits(),
                job.getLocation(),
                job.getCity(),
                job.getAddress(),
                job.getJobType(),
                job.getExperienceLevel(),
                job.getMinSalary(),
                job.getMaxSalary(),
                job.getIsNegotiable(),
                job.getDeadline(),
                job.getStatus() != null ? job.getStatus().name() : null,
                job.getRecruiterUserId(),
                job.getSkills() != null ? job.getSkills() : List.of(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
