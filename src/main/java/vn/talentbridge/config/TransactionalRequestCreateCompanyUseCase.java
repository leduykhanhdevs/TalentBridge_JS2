package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.RequestCreateCompanyCommand;
import vn.talentbridge.core.application.port.in.RequestCreateCompanyUseCase;

public class TransactionalRequestCreateCompanyUseCase
        implements RequestCreateCompanyUseCase {

    private final RequestCreateCompanyUseCase delegate;

    public TransactionalRequestCreateCompanyUseCase(
            RequestCreateCompanyUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public CompanyResult requestCreateCompany(
            Long userId,
            RequestCreateCompanyCommand command
    ) {
        return delegate.requestCreateCompany(userId, command);
    }
}
