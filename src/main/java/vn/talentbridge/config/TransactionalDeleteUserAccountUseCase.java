package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.port.in.DeleteUserAccountUseCase;

public class TransactionalDeleteUserAccountUseCase implements DeleteUserAccountUseCase {
    private final DeleteUserAccountUseCase delegate;

    public TransactionalDeleteUserAccountUseCase(DeleteUserAccountUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteAccount(Long accountUserId, Long adminUserId, boolean blockIdentifiers) {
        delegate.deleteAccount(accountUserId, adminUserId, blockIdentifiers);
    }
}
