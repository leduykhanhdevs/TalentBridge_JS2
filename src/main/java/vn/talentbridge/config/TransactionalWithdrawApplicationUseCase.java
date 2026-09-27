package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.ApplicationResult;
import vn.talentbridge.core.application.port.in.WithdrawApplicationUseCase;

public class TransactionalWithdrawApplicationUseCase implements WithdrawApplicationUseCase {

    private final WithdrawApplicationUseCase delegate;

    public TransactionalWithdrawApplicationUseCase(WithdrawApplicationUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ApplicationResult withdraw(Long userId, Long applicationId) {
        return delegate.withdraw(userId, applicationId);
    }
}
