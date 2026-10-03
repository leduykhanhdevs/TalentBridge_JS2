package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.JobDetailResult;
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
public class JobAdminResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private Long companyId;
    private String companyName;
    private String jobType;
    private String experienceLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String city;
    private Long recruiterUserId;
    private List<String> skills;
    private JobStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    public static JobAdminResponse from(JobDetailResult result) {
        return JobAdminResponse.builder()
                .id(result.id())
                .title(result.title())
                .description(result.description())
                .requirements(result.requirements())
                .benefits(result.benefits())
                .companyId(result.companyId())
                .companyName(result.companyName())
                .jobType(result.jobType())
                .experienceLevel(result.experienceLevel())
                .salaryMin(result.minSalary())
                .salaryMax(result.maxSalary())
                .city(result.city())
                .recruiterUserId(result.recruiterUserId())
                .skills(result.skills())
                .status(result.status() != null ? JobStatus.valueOf(result.status()) : null)
                .deadline(result.deadline())
                .createdAt(result.createdAt())
                .build();
    }
}
