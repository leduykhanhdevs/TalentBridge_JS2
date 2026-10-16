package vn.talentbridge.core.application.dto;

import java.util.List;

/**
 * Advisory similarity between a job and explicit candidate evidence.
 * Scores are in the range 0..1 except matchPercentage, which is 0..100.
 */
public record CandidateJobMatchResult(
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        Double matchPercentage,
        Double directKeywordScore,
        Double contextualSimilarityScore,
        Double inferredCapabilityScore,
        List<String> inferredCapabilities,
        List<String> verificationQuestions,
        List<String> matchingStrengths,
        List<String> missingCriticalSkills,
        String recommendation,
        String analysisSummary,
        AiMatchingSource source
) {
    public CandidateJobMatchResult {
        inferredCapabilities = inferredCapabilities == null ? List.of() : List.copyOf(inferredCapabilities);
        verificationQuestions = verificationQuestions == null ? List.of() : List.copyOf(verificationQuestions);
        matchingStrengths = matchingStrengths == null ? List.of() : List.copyOf(matchingStrengths);
        missingCriticalSkills = missingCriticalSkills == null ? List.of() : List.copyOf(missingCriticalSkills);
    }
}
