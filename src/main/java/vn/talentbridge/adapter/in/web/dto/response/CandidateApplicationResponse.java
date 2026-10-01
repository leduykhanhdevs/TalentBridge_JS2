package vn.talentbridge.adapter.in.web.dto.response;

import vn.talentbridge.core.application.dto.CandidateApplicationResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CandidateApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long companyId,
        String companyName,
        String companyLogo,
        String location,
        String city,
        String jobType,
        String experienceLevel,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        Boolean isNegotiable,
        Long resumeId,
        String resumeFileName,
        String resumeFileUrl,
        String coverLetter,
        String currentStage,
        String status,
        BigDecimal aiMatchScore,
        LocalDateTime appliedAt
) {
    public static CandidateApplicationResponse from(CandidateApplicationResult result) {
        return new CandidateApplicationResponse(
                result.id(),
                result.jobId(),
                result.jobTitle(),
                result.companyId(),
                result.companyName(),
                result.companyLogo(),
                result.location(),
                result.city(),
                result.jobType(),
                result.experienceLevel(),
                result.minSalary(),
                result.maxSalary(),
                result.isNegotiable(),
                result.resumeId(),
                result.resumeFileName(),
                result.resumeFileUrl(),
                result.coverLetter(),
                result.currentStage(),
                result.status(),
                result.aiMatchScore(),
                result.appliedAt()
        );
    }
}
