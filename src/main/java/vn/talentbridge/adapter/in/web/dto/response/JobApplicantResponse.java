package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.talentbridge.core.application.dto.JobApplicantResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicantResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String title;
    private Integer yearsOfExperience;
    private String city;
    private String coverLetter;
    private String currentStage;
    private String status;
    private BigDecimal aiMatchScore;
    private LocalDateTime appliedAt;

    public static JobApplicantResponse from(JobApplicantResult result) {
        return JobApplicantResponse.builder()
                .id(result.getId())
                .jobId(result.getJobId())
                .candidateId(result.getCandidateId())
                .fullName(result.getFullName())
                .email(result.getEmail())
                .phone(result.getPhone())
                .avatar(result.getAvatar())
                .title(result.getTitle())
                .yearsOfExperience(result.getYearsOfExperience())
                .city(result.getCity())
                .coverLetter(result.getCoverLetter())
                .currentStage(result.getCurrentStage())
                .status(result.getStatus())
                .aiMatchScore(result.getAiMatchScore())
                .appliedAt(result.getAppliedAt())
                .build();
    }
}
