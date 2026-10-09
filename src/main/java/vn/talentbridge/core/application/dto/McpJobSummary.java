package vn.talentbridge.core.application.dto;

public record McpJobSummary(
        Long id,
        String title,
        String company,
        String location,
        String experience
) {
}
