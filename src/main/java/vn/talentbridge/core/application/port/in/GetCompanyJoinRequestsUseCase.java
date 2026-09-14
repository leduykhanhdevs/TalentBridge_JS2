package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;

public interface GetCompanyJoinRequestsUseCase {
    List<CompanyJoinRequestResult> getCompanyJoinRequests(Long userId, CompanyJoinRequestStatus status, int page, int size);
    long countCompanyJoinRequests(Long userId, CompanyJoinRequestStatus status);
}
