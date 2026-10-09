package vn.talentbridge.core.domain.model;

public record JobQualityCriterion(
        String name,
        int points,
        int maximumPoints,
        boolean satisfied,
        String evidence
) {
}
