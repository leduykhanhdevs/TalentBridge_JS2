package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.JobResult;

import java.util.List;

public interface GetCompanyJobsUseCase {
    List<JobResult> getCompanyJobs(Long recruiterUserId);
}
