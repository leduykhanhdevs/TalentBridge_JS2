package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;

public interface LoginUseCase {
    AuthResult login(LoginCommand command);
}