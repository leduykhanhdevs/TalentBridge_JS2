package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;

public interface AssessCandidateForJobUseCase {
    CandidateJobAssessmentResult assess(Long recruiterUserId, Long jobId, Long candidateId);
}
