package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.RequestCreateCompanyCommand;

public interface RequestCreateCompanyUseCase {
    CompanyResult requestCreateCompany(Long userId, RequestCreateCompanyCommand command);
}
