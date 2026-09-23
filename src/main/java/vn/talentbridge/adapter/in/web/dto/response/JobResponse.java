package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

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
    private List<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobResponse from(JobDetailResult result) {
        if (result == null) {
            return null;
        }
        return JobResponse.builder()
                .id(result.id())
                .companyId(result.companyId())
                .companyName(result.companyName())
                .title(result.title())
                .description(result.description())
                .requirements(result.requirements())
                .benefits(result.benefits())
                .location(result.location())
                .city(result.city())
                .address(result.address())
                .jobType(result.jobType())
                .experienceLevel(result.experienceLevel())
                .minSalary(result.minSalary())
                .maxSalary(result.maxSalary())
                .isNegotiable(result.isNegotiable())
                .deadline(result.deadline())
                .status(result.status() != null ? JobStatus.valueOf(result.status()) : null)
                .recruiterUserId(result.recruiterUserId())
                .skills(result.skills())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .build();
    }

    public static JobResponse from(Job job) {
        if (job == null) {
            return null;
        }
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .companyName(job.getCompanyName())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .location(job.getLocation())
                .city(job.getCity())
                .address(job.getAddress())
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .isNegotiable(job.getIsNegotiable())
                .deadline(job.getDeadline())
                .status(job.getStatus())
                .recruiterUserId(job.getRecruiterUserId())
                .skills(job.getSkills())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
