package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

public interface MatchApplicantUseCase {
    CandidateJobMatchResult matchApplicant(Long recruiterUserId, Long jobId, Long candidateId);
}
