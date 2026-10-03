package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.UpdateApplicantStatusCommand;

public interface UpdateApplicantStatusUseCase {

    JobApplicantResult updateStageAndStatus(Long recruiterUserId, Long jobId, Long applicationId, UpdateApplicantStatusCommand command);

    JobApplicantResult reopenApplication(Long recruiterUserId, Long jobId, Long applicationId, String reason);
}
