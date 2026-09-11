package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.RefreshTokenUseCase;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.exception.UserAccountLockedException;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {
    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final long tokenExpirationMs;

    public RefreshTokenUseCaseImpl(UserRepositoryPort userRepository,
                                  TokenProviderPort tokenProvider,
                                  long tokenExpirationMs) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidCredentialsException();
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new UserAccountLockedException();
        }

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(Enum::name)
                .orElse("ROLE_CANDIDATE");

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), primaryRole);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResult.of(newAccessToken, newRefreshToken, tokenExpirationMs / 1000, UserResult.from(user));
    }
}