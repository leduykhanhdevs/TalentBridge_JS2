package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.exception.UserAccountLockedException;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;

public class LoginUseCaseImpl implements LoginUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final long tokenExpirationMs;

    public LoginUseCaseImpl(UserRepositoryPort userRepository,
                            PasswordEncoderPort passwordEncoder,
                            TokenProviderPort tokenProvider,
                            long tokenExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    @Override
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            throw new UserAccountLockedException();
        }

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(Enum::name)
                .orElse("ROLE_CANDIDATE");

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), primaryRole);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResult.of(accessToken, refreshToken, tokenExpirationMs / 1000, UserResult.from(user));
    }
}