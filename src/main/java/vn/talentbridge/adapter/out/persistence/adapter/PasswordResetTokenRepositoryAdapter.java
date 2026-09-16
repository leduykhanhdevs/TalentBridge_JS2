package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.PasswordResetTokenJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.PasswordResetTokenJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.domain.model.PasswordResetToken;

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

        return toDomain(tokenJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return tokenJpaRepository.findByTokenHash(tokenHash)
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
            LocalDateTime usedAt
    ) {
        return tokenJpaRepository.markUsedIfUsable(
                tokenHash,
                usedAt
        ) == 1;
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
                entity.getUsedAt()
        );
    }
}