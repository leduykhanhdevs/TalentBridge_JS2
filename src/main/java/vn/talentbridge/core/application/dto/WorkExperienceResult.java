package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.WorkExperience;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WorkExperienceResult(
        Long id,
        Long candidateId,
        String companyName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isCurrent,
        String description,
        String achievements,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkExperienceResult from(WorkExperience exp) {
        return new WorkExperienceResult(
                exp.getId(),
                exp.getCandidateId(),
                exp.getCompanyName(),
                exp.getPosition(),
                exp.getStartDate(),
                exp.getEndDate(),
                exp.getIsCurrent(),
                exp.getDescription(),
                exp.getAchievements(),
                exp.getCreatedAt(),
                exp.getUpdatedAt()
        );
    }
}
