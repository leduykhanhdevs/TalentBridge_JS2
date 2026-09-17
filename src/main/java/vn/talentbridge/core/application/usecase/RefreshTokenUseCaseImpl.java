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
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.domain.model.AuthSession;
import java.time.LocalDateTime;
import java.time.Duration;


public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {
    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;
    private final long tokenExpirationMs;

    public RefreshTokenUseCaseImpl(
            UserRepositoryPort userRepository,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            long tokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.authSessionRepository = authSessionRepository;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidCredentialsException();
        }

        if (!"refresh".equals(tokenProvider.getTokenTypeFromToken(refreshToken))) {
            throw new InvalidCredentialsException();
        }

        String sessionId = tokenProvider.getSessionIdFromToken(refreshToken);
        if (sessionId == null || sessionId.isBlank()) {
            throw new InvalidCredentialsException();
        }

        LocalDateTime now = LocalDateTime.now();

        AuthSession authSession = authSessionRepository
                .findActiveBySessionId(sessionId, now)
                .orElseThrow(InvalidCredentialsException::new);

        if (!tokenProvider.matchesRefreshTokenHash(
                refreshToken,
                authSession.getRefreshTokenHash()
        )) {
            throw new InvalidCredentialsException();
        }

        Long tokenUserId = tokenProvider.getUserIdFromToken(refreshToken);
        if (tokenUserId == null
                || authSession.getUserId() == null
                || !tokenUserId.equals(authSession.getUserId())) {
            throw new InvalidCredentialsException();
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!tokenUserId.equals(user.getId())) {
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

        LocalDateTime sessionExpiresAt =
                authSession.getExpiresAt();

        LocalDateTime configuredAccessExpiresAt =
                now.plus(Duration.ofMillis(tokenExpirationMs));

        LocalDateTime accessTokenExpiresAt =
                configuredAccessExpiresAt.isBefore(sessionExpiresAt)
                        ? configuredAccessExpiresAt
                        : sessionExpiresAt;

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                primaryRole,
                sessionId,
                accessTokenExpiresAt
        );

        String newRefreshToken = tokenProvider.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                sessionId,
                sessionExpiresAt
        );

        boolean rotated = authSessionRepository.rotateRefreshTokenIfActive(
                sessionId,
                user.getId(),
                authSession.getRefreshTokenHash(),
                tokenProvider.hashRefreshToken(newRefreshToken),
                LocalDateTime.now()
        );

        if (!rotated) {
            throw new InvalidCredentialsException();
        }

        long expiresInSeconds = Math.max(
                0L,
                Duration.between(
                        now,
                        accessTokenExpiresAt
                ).toSeconds()
        );

        return AuthResult.of(
                newAccessToken,
                newRefreshToken,
                expiresInSeconds,
                UserResult.from(user)
        );
    }
}