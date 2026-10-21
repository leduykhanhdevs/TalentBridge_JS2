package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.LogoutUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.domain.exception.InvalidSessionException;

import java.time.LocalDateTime;

public class LogoutUseCaseImpl implements LogoutUseCase {

    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;

    public LogoutUseCaseImpl(
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository
    ) {
        this.tokenProvider = tokenProvider;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        SessionIdentity accessSession = readSessionIdentity(accessToken, "access");
        SessionIdentity refreshSession = readSessionIdentity(refreshToken, "refresh");

        if (accessSession == null && refreshSession == null) {
            throw new InvalidSessionException();
        }

        if (accessSession != null
                && refreshSession != null
                && !accessSession.equals(refreshSession)) {
            throw new InvalidSessionException();
        }

        SessionIdentity session = accessSession != null ? accessSession : refreshSession;
        LocalDateTime now = LocalDateTime.now();
        boolean revoked = accessSession != null
                ? authSessionRepository.revokeActiveSession(
                        session.sessionId(),
                        session.userId(),
                        now
                )
                : authSessionRepository.revokeActiveSessionByRefreshTokenHash(
                        session.sessionId(),
                        session.userId(),
                        tokenProvider.hashRefreshToken(refreshToken),
                        now
                );

        if (!revoked) {
            throw new InvalidSessionException();
        }
    }

    private SessionIdentity readSessionIdentity(String token, String expectedType) {
        if (token == null || token.isBlank() || !tokenProvider.validateToken(token)) {
            return null;
        }

        try {
            String tokenType = tokenProvider.getTokenTypeFromToken(token);
            String sessionId = tokenProvider.getSessionIdFromToken(token);
            Long userId = tokenProvider.getUserIdFromToken(token);
            if (!expectedType.equals(tokenType)
                    || sessionId == null
                    || sessionId.isBlank()
                    || userId == null) {
                return null;
            }
            return new SessionIdentity(sessionId, userId);
        } catch (RuntimeException exception) {
            // Token có thể vừa hết hạn giữa các lần đọc.
            return null;
        }
    }

    private record SessionIdentity(String sessionId, Long userId) {
    }
}
