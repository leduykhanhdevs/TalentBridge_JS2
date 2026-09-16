package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.AuthSessionJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.AuthSessionJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.domain.model.AuthSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class AuthSessionRepositoryAdapter
        implements AuthSessionRepositoryPort {

    private final AuthSessionJpaRepository authSessionJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public AuthSessionRepositoryAdapter(
            AuthSessionJpaRepository authSessionJpaRepository,
            UserJpaRepository userJpaRepository
    ) {
        this.authSessionJpaRepository = authSessionJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public AuthSession save(AuthSession authSession) {
        AuthSessionJpaEntity entity;

        if (authSession.getId() == null) {
            entity = new AuthSessionJpaEntity();
        } else {
            entity = authSessionJpaRepository.findById(authSession.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Auth session not found: " + authSession.getId()
                            ));
        }

        UserJpaEntity user = userJpaRepository.findById(authSession.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + authSession.getUserId()
                        ));

        entity.setSessionId(authSession.getSessionId());
        entity.setUser(user);
        entity.setRefreshTokenHash(authSession.getRefreshTokenHash());
        entity.setExpiresAt(authSession.getExpiresAt());
        entity.setRevokedAt(authSession.getRevokedAt());

        return toDomain(authSessionJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthSession> findBySessionId(String sessionId) {
        return authSessionJpaRepository.findBySessionId(sessionId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthSession> findActiveBySessionId(
            String sessionId,
            LocalDateTime now
    ) {
        return authSessionJpaRepository
                .findBySessionIdAndRevokedAtIsNullAndExpiresAtAfter(
                        sessionId,
                        now
                )
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthSession> findAllByUserId(Long userId) {
        return authSessionJpaRepository.findAllByUser_Id(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean rotateRefreshTokenIfActive(
            String sessionId,
            Long userId,
            String expectedHash,
            String newHash,
            LocalDateTime now
    ) {
        return authSessionJpaRepository.rotateRefreshTokenIfActive(
                sessionId,
                userId,
                expectedHash,
                newHash,
                now
        ) == 1;
    }

    private AuthSession toDomain(AuthSessionJpaEntity entity) {
        return new AuthSession(
                entity.getId(),
                entity.getSessionId(),
                entity.getUser().getId(),
                entity.getRefreshTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }
}