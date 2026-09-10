package vn.talentbridge.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.modules.job.entity.Job;
import vn.talentbridge.modules.job.enums.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAdminResponse {

    private Long id;
    private String title;
    private Long companyId;
    private String companyName;
    private String jobType;
    private String experienceLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Boolean isNegotiable;
    private String city;
    private JobStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    public static JobAdminResponse from(Job job) {
        return JobAdminResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyId(job.getCompany() != null ? job.getCompany().getId() : null)
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .isNegotiable(job.getIsNegotiable())
                .city(job.getCity())
                .status(job.getStatus())
                .deadline(job.getDeadline())
                .createdAt(job.getCreatedAt())
                .build();
    }
}