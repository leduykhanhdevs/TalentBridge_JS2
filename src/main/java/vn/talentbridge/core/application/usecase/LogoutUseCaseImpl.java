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
    public void logout(String accessToken) {
        if (accessToken == null
                || accessToken.isBlank()
                || !tokenProvider.validateToken(accessToken)) {
            throw new InvalidSessionException();
        }

        String tokenType;
        String sessionId;
        Long userId;

        try {
            tokenType = tokenProvider.getTokenTypeFromToken(accessToken);
            sessionId = tokenProvider.getSessionIdFromToken(accessToken);
            userId = tokenProvider.getUserIdFromToken(accessToken);
        } catch (RuntimeException exception) {
            // Token có thể vừa hết hạn giữa các lần đọc.
            throw new InvalidSessionException();
        }

        if (!"access".equals(tokenType)
                || sessionId == null
                || sessionId.isBlank()
                || userId == null) {
            throw new InvalidSessionException();
        }

        boolean revoked = authSessionRepository.revokeActiveSession(
                sessionId,
                userId,
                LocalDateTime.now()
        );

        if (!revoked) {
            throw new InvalidSessionException();
        }
    }
}