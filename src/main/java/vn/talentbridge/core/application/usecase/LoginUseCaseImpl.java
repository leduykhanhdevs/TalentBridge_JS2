package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.LoginAttemptTrackerPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.exception.TooManyLoginAttemptsException;
import vn.talentbridge.core.domain.exception.UserAccountLockedException;
import vn.talentbridge.core.domain.model.AuthSession;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class LoginUseCaseImpl implements LoginUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;
    private final LoginAttemptTrackerPort loginAttemptTracker;
    private final long tokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public LoginUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            LoginAttemptTrackerPort loginAttemptTracker,
            long tokenExpirationMs,
            long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authSessionRepository = authSessionRepository;
        this.loginAttemptTracker = loginAttemptTracker;
        this.tokenExpirationMs = tokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public AuthResult login(LoginCommand command) {
        String normalizedEmail = command.email()
                .trim()
                .toLowerCase(Locale.ROOT);

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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionExpiresAt = now.plus(
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        LocalDateTime configuredAccessExpiresAt = now.plus(
                Duration.ofMillis(tokenExpirationMs)
        );

        LocalDateTime accessTokenExpiresAt =
                configuredAccessExpiresAt.isBefore(sessionExpiresAt)
                        ? configuredAccessExpiresAt
                        : sessionExpiresAt;

        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                primaryRole,
                sessionId,
                accessTokenExpiresAt
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                sessionId,
                sessionExpiresAt
        );

        AuthSession authSession = new AuthSession(
                null,
                sessionId,
                user.getId(),
                tokenProvider.hashRefreshToken(refreshToken),
                now,
                sessionExpiresAt,
                null
        );

        authSessionRepository.save(authSession);

        long expiresInSeconds = Math.max(
                0L,
                Duration.between(
                        now,
                        accessTokenExpiresAt
                ).toSeconds()
        );

        return AuthResult.of(
                accessToken,
                refreshToken,
                expiresInSeconds,
                UserResult.from(user)
        );
    }

    private RuntimeException invalidCredentials(String identifier) {
        if (loginAttemptTracker.recordFailure(identifier)) {
            return new TooManyLoginAttemptsException();
        }
        return new InvalidCredentialsException();
    }
}
