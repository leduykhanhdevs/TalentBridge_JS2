package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.UpdateApplicantStatusCommand;
import vn.talentbridge.core.application.port.in.UpdateApplicantStatusUseCase;

public class TransactionalUpdateApplicantStatusUseCase implements UpdateApplicantStatusUseCase {
    private final UpdateApplicantStatusUseCase delegate;

    public TransactionalUpdateApplicantStatusUseCase(UpdateApplicantStatusUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobApplicantResult updateStageAndStatus(Long recruiterUserId, Long jobId, Long applicationId,
                                                   UpdateApplicantStatusCommand command) {
        return delegate.updateStageAndStatus(recruiterUserId, jobId, applicationId, command);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobApplicantResult reopenApplication(Long recruiterUserId, Long jobId, Long applicationId, String reason) {
        return delegate.reopenApplication(recruiterUserId, jobId, applicationId, reason);
    }
}
