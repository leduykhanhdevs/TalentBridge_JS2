package vn.talentbridge.modules.auth.service;

import vn.talentbridge.modules.auth.dto.request.LoginRequest;
import vn.talentbridge.modules.auth.dto.request.RefreshTokenRequest;
import vn.talentbridge.modules.auth.dto.request.RegisterRequest;
import vn.talentbridge.modules.auth.dto.response.AuthResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserResponse getCurrentUser(String email);
}