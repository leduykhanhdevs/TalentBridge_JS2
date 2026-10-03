package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.port.in.UpdateCompanyStatusUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;

public class TransactionalUpdateCompanyStatusUseCase implements UpdateCompanyStatusUseCase {
    private final UpdateCompanyStatusUseCase delegate;

    public TransactionalUpdateCompanyStatusUseCase(UpdateCompanyStatusUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status, String reason) {
        return delegate.updateCompanyStatus(companyId, status, reason);
    }
}
