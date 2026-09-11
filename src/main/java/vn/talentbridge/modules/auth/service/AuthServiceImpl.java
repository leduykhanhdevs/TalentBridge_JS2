package vn.talentbridge.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.GetCurrentUserUseCase;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.in.RefreshTokenUseCase;
import vn.talentbridge.core.application.port.in.RegisterUseCase;
import vn.talentbridge.modules.auth.dto.request.LoginRequest;
import vn.talentbridge.modules.auth.dto.request.RefreshTokenRequest;
import vn.talentbridge.modules.auth.dto.request.RegisterRequest;
import vn.talentbridge.modules.auth.dto.response.AuthResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.user.enums.UserStatus;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @Override
    public AuthResponse register(RegisterRequest request) {
        AuthResult result = registerUseCase.register(new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getPhone(),
                request.getRole()
        ));
        return toAuthResponse(result);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AuthResult result = loginUseCase.login(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return toAuthResponse(result);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        AuthResult result = refreshTokenUseCase.refreshToken(request.getRefreshToken());
        return toAuthResponse(result);
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        UserResult result = getCurrentUserUseCase.getCurrentUser(email);
        return toUserResponse(result);
    }

    private AuthResponse toAuthResponse(AuthResult result) {
        return AuthResponse.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .tokenType(result.tokenType())
                .expiresInMs(result.expiresIn() * 1000)
                .user(toUserResponse(result.user()))
                .build();
    }

    private UserResponse toUserResponse(UserResult user) {
        return UserResponse.builder()
                .id(user.id())
                .email(user.email())
                .fullName(user.fullName())
                .phone(user.phoneNumber())
                .avatarUrl(user.avatarUrl())
                .status(user.status() != null ? UserStatus.valueOf(user.status()) : null)
                .roles(user.roles())
                .createdAt(user.createdAt())
                .build();
    }
}