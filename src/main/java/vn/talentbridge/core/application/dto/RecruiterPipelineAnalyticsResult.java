package vn.talentbridge.core.application.dto;

import java.util.List;

public record RecruiterPipelineAnalyticsResult(
        Long jobId,
        String jobTitle,
        long totalApplications,
        List<PipelineStageStatistic> stages,
        String interpretation
) {
    public RecruiterPipelineAnalyticsResult {
        stages = stages == null ? List.of() : List.copyOf(stages);
    }
}
