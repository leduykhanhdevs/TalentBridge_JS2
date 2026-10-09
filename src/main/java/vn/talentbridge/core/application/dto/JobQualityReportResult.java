package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.JobQualityAssessment;
import vn.talentbridge.core.domain.model.JobQualityCriterion;

import java.util.List;

public record JobQualityReportResult(
        Long jobId,
        String jobTitle,
        String jobStatus,
        int qualityScore,
        String qualityLabel,
        List<JobQualityCriterion> criteria,
        List<String> trustSignals,
        List<String> improvementSuggestions,
        String limitation
) {
    public static JobQualityReportResult from(Long jobId, String jobTitle, String jobStatus,
                                              JobQualityAssessment assessment) {
        return new JobQualityReportResult(jobId, jobTitle, jobStatus, assessment.qualityScore(),
                assessment.qualityLabel(), assessment.criteria(), assessment.trustSignals(),
                assessment.improvementSuggestions(), assessment.limitation());
    }
}
