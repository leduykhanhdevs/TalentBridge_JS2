package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ChangePasswordCommand;

public interface ChangePasswordUseCase {
    void changePassword(Long userId, ChangePasswordCommand command);
}
