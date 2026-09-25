package vn.talentbridge.core.application.dto;

import java.time.LocalDateTime;

public record ApplicationResult(
        Long id,
        Long jobId,
        Long candidateId,
        Long resumeId,
        String coverLetter,
        String currentStage,
        String status,
        LocalDateTime createdAt
) {
}