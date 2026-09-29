package vn.talentbridge.core.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.talentbridge.core.domain.model.JobApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicantResult {
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

    public static JobApplicantResult from(JobApplication app) {
        return JobApplicantResult.builder()
                .id(app.getId())
                .jobId(app.getJobId())
                .candidateId(app.getCandidateId())
                .fullName(app.getCandidateFullName())
                .email(app.getCandidateEmail())
                .phone(app.getCandidatePhone())
                .avatar(app.getCandidateAvatarUrl())
                .title(app.getCandidateTitle())
                .yearsOfExperience(app.getCandidateExperienceYears())
                .city(app.getCandidateCity())
                .coverLetter(app.getCoverLetter())
                .currentStage(app.getCurrentStage())
                .status(app.getStatus())
                .aiMatchScore(app.getAiMatchScore())
                .appliedAt(app.getCreatedAt())
                .build();
    }
}
