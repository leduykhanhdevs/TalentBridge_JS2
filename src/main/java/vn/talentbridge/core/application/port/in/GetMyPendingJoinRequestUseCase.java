package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;

import java.util.Optional;

public interface GetMyPendingJoinRequestUseCase {

    Optional<CompanyJoinRequestResult> getMyPendingJoinRequest(Long userId);
}
