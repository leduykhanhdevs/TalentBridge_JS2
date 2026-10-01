package vn.talentbridge.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CandidateApplicationResult(
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
) {}
