package vn.talentbridge.core.application.dto;

import java.util.List;

public record McpCandidateProfileResult(
        Long candidateId,
        String name,
        String title,
        Integer experienceYears,
        String city,
        String summary,
        List<String> skills
) {
    public McpCandidateProfileResult {
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
