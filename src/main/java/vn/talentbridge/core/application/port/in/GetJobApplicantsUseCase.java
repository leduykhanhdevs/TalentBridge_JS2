package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.JobApplicantResult;

import java.util.List;

public interface GetJobApplicantsUseCase {
    List<JobApplicantResult> getJobApplicants(Long recruiterUserId, Long jobId);
}
