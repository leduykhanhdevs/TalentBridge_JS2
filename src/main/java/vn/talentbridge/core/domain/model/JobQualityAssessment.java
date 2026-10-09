package vn.talentbridge.core.domain.model;

import java.util.List;

public record JobQualityAssessment(
        int qualityScore,
        String qualityLabel,
        List<JobQualityCriterion> criteria,
        List<String> trustSignals,
        List<String> improvementSuggestions,
        String limitation
) {
    public JobQualityAssessment {
        criteria = criteria == null ? List.of() : List.copyOf(criteria);
        trustSignals = trustSignals == null ? List.of() : List.copyOf(trustSignals);
        improvementSuggestions = improvementSuggestions == null ? List.of() : List.copyOf(improvementSuggestions);
    }
}
