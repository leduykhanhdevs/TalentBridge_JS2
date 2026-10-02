package vn.talentbridge.core.application.dto;

import java.util.List;

public record ParsedCvResult(
        String fullName,
        String email,
        String phone,
        String title,
        String city,
        String summary,
        List<String> skills,
        List<ParsedExperienceItem> experiences,
        String rawText
) {
    public record ParsedExperienceItem(
            String companyName,
            String position,
            String startDate,
            String endDate,
            Boolean isCurrent,
            String description
    ) {}
}
