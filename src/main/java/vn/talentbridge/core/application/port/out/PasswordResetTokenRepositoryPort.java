package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    int invalidateUnusedByUserId(
            Long userId,
            LocalDateTime usedAt
    );

    boolean markUsedIfUsable(
            String tokenHash,
            LocalDateTime usedAt
    );
}