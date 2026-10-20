package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.PasswordResetToken.Purpose;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findLatestActiveByUserIdAndPurpose(
            Long userId,
            Purpose purpose,
            LocalDateTime now
    );

    int invalidateUnusedByUserId(
            Long userId,
            LocalDateTime usedAt
    );

    boolean markUsedIfUsable(
            String tokenHash,
            Purpose purpose,
            LocalDateTime usedAt
    );

    boolean incrementFailedAttempts(
            Long tokenId,
            LocalDateTime attemptedAt,
            int attemptLimit
    );

    void markUsedIfAttemptLimitReached(
            Long tokenId,
            LocalDateTime usedAt,
            int attemptLimit
    );
}
