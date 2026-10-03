package vn.talentbridge.core.application.dto;

public record MyJobStatsResult(
        long total,
        long draft,
        long pending,
        long active,
        long rejected,
        long expired,
        long closed
) {
}
