package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.SubmitJoinCompanyRequestCommand;

public interface SubmitJoinCompanyRequestUseCase {
    CompanyJoinRequestResult submitJoinRequest(Long userId, Long companyId, SubmitJoinCompanyRequestCommand command);
}
