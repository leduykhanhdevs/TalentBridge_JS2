package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AuthResult;

public interface RefreshTokenUseCase {
    AuthResult refreshToken(String refreshToken);
}