package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

public interface MatchApplicantUseCase {
    CandidateJobMatchResult matchApplicant(Long recruiterUserId, Long jobId, Long candidateId);

    /** Verifies recruiter ownership and application membership before any private CV content is read. */
    void assertCanAssessApplicant(Long recruiterUserId, Long jobId, Long candidateId);

    /** Revalidates access and matches supplied CV text locally, without invoking a third-party AI provider. */
    CandidateJobMatchResult matchApplicantLocally(
            Long recruiterUserId, Long jobId, Long candidateId, String resumeText);
}
