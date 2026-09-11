package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.domain.vo.JobStatus;

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
    private String city;
    private JobStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    public static JobAdminResponse from(JobResult result) {
        return JobAdminResponse.builder()
                .id(result.id())
                .title(result.title())
                .companyId(result.companyId())
                .companyName(result.companyName())
                .jobType(result.jobType())
                .experienceLevel(result.experienceLevel())
                .salaryMin(result.minSalary())
                .salaryMax(result.maxSalary())
                .city(result.location())
                .status(result.status() != null ? JobStatus.valueOf(result.status()) : null)
                .deadline(result.deadline())
                .createdAt(result.createdAt())
                .build();
    }
}