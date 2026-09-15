package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.exception.UserAccountLockedException;
import vn.talentbridge.core.domain.model.AuthSession;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoginUseCaseImpl implements LoginUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;
    private final long tokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public LoginUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            long tokenExpirationMs,
            long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authSessionRepository = authSessionRepository;
        this.tokenExpirationMs = tokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
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

        LocalDateTime now = LocalDateTime.now();
        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                primaryRole,
                sessionId
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                sessionId
        );

        AuthSession authSession = new AuthSession(
                null,
                sessionId,
                user.getId(),
                passwordEncoder.encode(refreshToken),
                now,
                now.plus(Duration.ofMillis(refreshTokenExpirationMs)),
                null
        );

        authSessionRepository.save(authSession);

        return AuthResult.of(accessToken, refreshToken, tokenExpirationMs / 1000, UserResult.from(user));
    }
}