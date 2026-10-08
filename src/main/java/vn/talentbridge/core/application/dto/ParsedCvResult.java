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
        String rawText,
        CvParsingSource processingSource
) {
    public ParsedCvResult(
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
        this(fullName, email, phone, title, city, summary, skills, experiences, rawText, CvParsingSource.RULE_BASED);
    }

    public ParsedCvResult {
        processingSource = processingSource == null ? CvParsingSource.RULE_BASED : processingSource;
    }

    public record ParsedExperienceItem(
            String companyName,
            String position,
            String startDate,
            String endDate,
            Boolean isCurrent,
            String description
    ) {}
}
