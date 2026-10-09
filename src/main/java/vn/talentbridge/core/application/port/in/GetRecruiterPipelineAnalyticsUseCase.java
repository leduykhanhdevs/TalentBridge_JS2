package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.RecruiterPipelineAnalyticsResult;

public interface GetRecruiterPipelineAnalyticsUseCase {
    RecruiterPipelineAnalyticsResult getForJob(Long recruiterUserId, Long jobId);
}
