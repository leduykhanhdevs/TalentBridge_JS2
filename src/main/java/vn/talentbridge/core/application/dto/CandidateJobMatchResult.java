package vn.talentbridge.core.application.dto;

import java.util.List;

public record CandidateJobMatchResult(
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        Double matchPercentage,
        Double directKeywordScore,
        Double inferredCapabilityScore,
        List<String> inferredCapabilities,
        List<String> matchingStrengths,
        List<String> missingCriticalSkills,
        String recommendation,
        String analysisSummary,
        AiMatchingSource source
) {
    public CandidateJobMatchResult {
        inferredCapabilities = inferredCapabilities == null ? List.of() : List.copyOf(inferredCapabilities);
        matchingStrengths = matchingStrengths == null ? List.of() : List.copyOf(matchingStrengths);
        missingCriticalSkills = missingCriticalSkills == null ? List.of() : List.copyOf(missingCriticalSkills);
    }
}
