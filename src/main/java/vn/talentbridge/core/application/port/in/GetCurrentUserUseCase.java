package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.UserResult;

public interface GetCurrentUserUseCase {
    UserResult getCurrentUser(String email);
}