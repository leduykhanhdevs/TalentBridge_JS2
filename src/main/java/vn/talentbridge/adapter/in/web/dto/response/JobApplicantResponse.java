package vn.talentbridge.adapter.in.web.dto.response;

import lombok.*;
import vn.talentbridge.core.application.dto.JobApplicantResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicantResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateAvatarUrl;
    private String candidateTitle;
    private Integer candidateExperienceYears;
    private String candidateCity;
    private Long resumeId;
    private String resumeUrl;
    private String resumeFileName;
    private String coverLetter;
    private String currentStage;
    private String status;
    private BigDecimal aiMatchScore;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private Double averageRating;
    private Integer notesCount;

    public static JobApplicantResponse from(JobApplicantResult result) {
        if (result == null) return null;
        return JobApplicantResponse.builder()
                .id(result.getId())
                .jobId(result.getJobId())
                .jobTitle(result.getJobTitle())
                .candidateId(result.getCandidateId())
                .candidateFullName(result.getCandidateFullName())
                .candidateEmail(result.getCandidateEmail())
                .candidatePhone(result.getCandidatePhone())
                .candidateAvatarUrl(result.getCandidateAvatarUrl())
                .candidateTitle(result.getCandidateTitle())
                .candidateExperienceYears(result.getCandidateExperienceYears())
                .candidateCity(result.getCandidateCity())
                .resumeId(result.getResumeId())
                .resumeUrl(result.getResumeUrl())
                .resumeFileName(result.getResumeFileName())
                .coverLetter(result.getCoverLetter())
                .currentStage(result.getCurrentStage())
                .status(result.getStatus())
                .aiMatchScore(result.getAiMatchScore())
                .appliedAt(result.getAppliedAt())
                .updatedAt(result.getUpdatedAt())
                .averageRating(result.getAverageRating())
                .notesCount(result.getNotesCount())
                .build();
    }
}
