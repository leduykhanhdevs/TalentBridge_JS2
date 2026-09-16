package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.AuthSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepositoryPort {

    AuthSession save(AuthSession authSession);

    Optional<AuthSession> findBySessionId(String sessionId);

    Optional<AuthSession> findActiveBySessionId(
            String sessionId,
            LocalDateTime now
    );

    List<AuthSession> findAllByUserId(Long userId);

    boolean rotateRefreshTokenIfActive(
            String sessionId,
            Long userId,
            String expectedHash,
            String newHash,
            LocalDateTime now
    );
    boolean revokeActiveSession(
            String sessionId,
            Long userId,
            LocalDateTime now
    );
}