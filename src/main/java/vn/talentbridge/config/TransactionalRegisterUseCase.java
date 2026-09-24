package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.port.in.RegisterUseCase;

public class TransactionalRegisterUseCase implements RegisterUseCase {

    private final RegisterUseCase delegate;

    public TransactionalRegisterUseCase(RegisterUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public AuthResult register(RegisterCommand command) {
        return delegate.register(command);
    }
}
