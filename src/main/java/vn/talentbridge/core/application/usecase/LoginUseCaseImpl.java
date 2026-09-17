package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.LoginAttemptTrackerPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.exception.TooManyLoginAttemptsException;
import vn.talentbridge.core.domain.exception.UserAccountLockedException;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;

import java.util.Locale;

public class LoginUseCaseImpl implements LoginUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final LoginAttemptTrackerPort loginAttemptTracker;
    private final long tokenExpirationMs;

    public LoginUseCaseImpl(UserRepositoryPort userRepository,
                            PasswordEncoderPort passwordEncoder,
                            TokenProviderPort tokenProvider,
                            LoginAttemptTrackerPort loginAttemptTracker,
                            long tokenExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptTracker = loginAttemptTracker;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    @Override
    public AuthResult login(LoginCommand command) {
        String normalizedEmail = command.email().trim().toLowerCase(Locale.ROOT);

        if (loginAttemptTracker.isBlocked(normalizedEmail)) {
            throw new TooManyLoginAttemptsException();
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> invalidCredentials(normalizedEmail));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw invalidCredentials(normalizedEmail);
        }

        if (!user.isActive()) {
            throw new UserAccountLockedException();
        }

        loginAttemptTracker.reset(normalizedEmail);

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(Enum::name)
                .orElse("ROLE_CANDIDATE");

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), primaryRole);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResult.of(accessToken, refreshToken, tokenExpirationMs / 1000, UserResult.from(user));
    }

    private RuntimeException invalidCredentials(String identifier) {
        if (loginAttemptTracker.recordFailure(identifier)) {
            return new TooManyLoginAttemptsException();
        }
        return new InvalidCredentialsException();
    }
}
