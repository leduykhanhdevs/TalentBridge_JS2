package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.domain.vo.CompanyStatus;

public interface UpdateCompanyStatusUseCase {
    CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status, String reason);
}
