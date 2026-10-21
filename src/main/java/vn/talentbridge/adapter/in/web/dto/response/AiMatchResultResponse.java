package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

import java.util.List;

/**
 * API response for project-defined, explainable three-dimension CV-to-job similarity.
 * It is not a probability of hiring or a formula from the cited AI-KM paper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResultResponse {
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private Double matchPercentage;
    private Double directKeywordScore;
    private Double contextualSimilarityScore;
    private Double inferredCapabilityScore;
    private List<String> inferredCapabilities;
    private List<String> verificationQuestions;
    private List<String> matchingStrengths;
    private List<String> missingCriticalSkills;
    private String recommendation;
    private String matchingSource;
    private String analysisSummary;

    public static AiMatchResultResponse from(CandidateJobMatchResult result) {
        if (result == null) return null;
        return AiMatchResultResponse.builder()
                .jobId(result.jobId())
                .jobTitle(result.jobTitle())
                .candidateId(result.candidateId())
                .candidateName(result.candidateName())
                .matchPercentage(result.matchPercentage())
                .directKeywordScore(result.directKeywordScore())
                .contextualSimilarityScore(result.contextualSimilarityScore())
                .inferredCapabilityScore(result.inferredCapabilityScore())
                .inferredCapabilities(result.inferredCapabilities())
                .verificationQuestions(result.verificationQuestions())
                .matchingStrengths(result.matchingStrengths())
                .missingCriticalSkills(result.missingCriticalSkills())
                .recommendation(result.recommendation())
                .analysisSummary(result.analysisSummary())
                .matchingSource(result.source() != null ? result.source().name() : "DETERMINISTIC")
                .build();
    }
}
