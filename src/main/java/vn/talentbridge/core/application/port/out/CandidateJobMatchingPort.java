package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

public interface CandidateJobMatchingPort {
    CandidateJobMatchResult match(Long jobId, Long candidateId);

    /** Matches using local deterministic rules only; implementations must not call external AI services. */
    CandidateJobMatchResult matchLocally(Long jobId, Long candidateId, String resumeText);
}
