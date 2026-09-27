package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ApplicationResult;

public interface WithdrawApplicationUseCase {
    ApplicationResult withdraw(Long userId, Long applicationId);
}
