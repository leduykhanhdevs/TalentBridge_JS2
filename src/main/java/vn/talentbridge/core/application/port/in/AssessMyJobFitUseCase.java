package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;

public interface AssessMyJobFitUseCase {
    CandidateJobAssessmentResult assess(Long candidateUserId, Long jobId);
}
