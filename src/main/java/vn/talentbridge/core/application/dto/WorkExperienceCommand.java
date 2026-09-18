package vn.talentbridge.core.application.dto;

import java.time.LocalDate;

public record WorkExperienceCommand(
        String companyName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isCurrent,
        String description,
        String achievements
) {
}
