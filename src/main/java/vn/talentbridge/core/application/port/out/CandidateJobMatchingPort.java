package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

public interface CandidateJobMatchingPort {
    CandidateJobMatchResult match(Long jobId, Long candidateId);
}
