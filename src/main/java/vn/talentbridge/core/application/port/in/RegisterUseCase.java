package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.RegisterCommand;

public interface RegisterUseCase {
    AuthResult register(RegisterCommand command);
}