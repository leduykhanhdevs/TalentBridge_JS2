package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.ReviewJoinRequestCommand;

public interface ReviewJoinRequestUseCase {
    CompanyJoinRequestResult reviewJoinRequest(Long reviewerUserId, Long requestId, ReviewJoinRequestCommand command);
}
