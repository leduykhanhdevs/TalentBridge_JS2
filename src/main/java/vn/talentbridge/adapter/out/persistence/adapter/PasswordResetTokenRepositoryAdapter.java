package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.PasswordResetTokenJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.PasswordResetTokenJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.PasswordResetToken.Purpose;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
public class PasswordResetTokenRepositoryAdapter
        implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository tokenJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public PasswordResetTokenRepositoryAdapter(
            PasswordResetTokenJpaRepository tokenJpaRepository,
            UserJpaRepository userJpaRepository
    ) {
        this.tokenJpaRepository = tokenJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity;

        if (token.getId() == null) {
            entity = new PasswordResetTokenJpaEntity();
        } else {
            entity = tokenJpaRepository.findById(token.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Password reset token not found: "
                                            + token.getId()
                            )
                    );
        }

        UserJpaEntity user = userJpaRepository.findById(token.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + token.getUserId()
                        )
                );

        entity.setUser(user);
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setPurpose(token.getPurpose());
        entity.setFailedAttempts(token.getFailedAttempts());

        return toDomain(tokenJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return tokenJpaRepository.findByTokenHash(tokenHash)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findLatestActiveByUserIdAndPurpose(
            Long userId,
            Purpose purpose,
            LocalDateTime now
    ) {
        return tokenJpaRepository
                .findFirstByUser_IdAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId,
                        purpose,
                        now
                )
                .map(this::toDomain);
    }

    @Override
    public int invalidateUnusedByUserId(
            Long userId,
            LocalDateTime usedAt
    ) {
        return tokenJpaRepository.invalidateUnusedByUserId(
                userId,
                usedAt
        );
    }

    @Override
    public boolean markUsedIfUsable(
            String tokenHash,
            Purpose purpose,
            LocalDateTime usedAt
    ) {
        return tokenJpaRepository.markUsedIfUsable(
                tokenHash,
                purpose,
                usedAt
        ) == 1;
    }

    @Override
    public boolean incrementFailedAttempts(
            Long tokenId,
            LocalDateTime attemptedAt,
            int attemptLimit
    ) {
        return tokenJpaRepository.incrementFailedAttempts(
                tokenId,
                attemptedAt,
                attemptLimit
        ) == 1;
    }

    @Override
    public void markUsedIfAttemptLimitReached(
            Long tokenId,
            LocalDateTime usedAt,
            int attemptLimit
    ) {
        tokenJpaRepository.markUsedIfAttemptLimitReached(
                tokenId,
                usedAt,
                attemptLimit
        );
    }

    private PasswordResetToken toDomain(
            PasswordResetTokenJpaEntity entity
    ) {
        return new PasswordResetToken(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getPurpose(),
                entity.getFailedAttempts()
        );
    }
}
