package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.port.in.GetRecruiterProfileUseCase;

public class TransactionalGetRecruiterProfileUseCase
        implements GetRecruiterProfileUseCase {

    private final GetRecruiterProfileUseCase delegate;

    public TransactionalGetRecruiterProfileUseCase(
            GetRecruiterProfileUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public RecruiterResult getProfile(Long userId) {
        return delegate.getProfile(userId);
    }
}
