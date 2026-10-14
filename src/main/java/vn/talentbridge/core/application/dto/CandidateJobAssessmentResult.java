package vn.talentbridge.core.application.dto;

import java.util.List;

public record CandidateJobAssessmentResult(
        CandidateJobMatchResult match,
        int profileCompletenessPercentage,
        List<String> missingProfileSections,
        String candidateOverallAssessment,
        String advisoryNotice,
        ResumeAssessmentStatus resumeAssessmentStatus
) {
    public CandidateJobAssessmentResult {
        missingProfileSections = missingProfileSections == null
                ? List.of() : List.copyOf(missingProfileSections);
        resumeAssessmentStatus = resumeAssessmentStatus == null
                ? ResumeAssessmentStatus.NO_RESUME : resumeAssessmentStatus;
    }
}
