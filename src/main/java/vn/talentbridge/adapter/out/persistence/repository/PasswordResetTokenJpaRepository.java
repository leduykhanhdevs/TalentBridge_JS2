package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.PasswordResetTokenJpaEntity;
import vn.talentbridge.core.domain.model.PasswordResetToken.Purpose;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenJpaRepository
        extends JpaRepository<PasswordResetTokenJpaEntity, Long> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    Optional<PasswordResetTokenJpaEntity>
    findFirstByUser_IdAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            Purpose purpose,
            LocalDateTime now
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update PasswordResetTokenJpaEntity t
            set t.usedAt = :usedAt,
                t.updatedAt = :usedAt
            where t.user.id = :userId
              and t.usedAt is null
            """)
    int invalidateUnusedByUserId(
            @Param("userId") Long userId,
            @Param("usedAt") LocalDateTime usedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update PasswordResetTokenJpaEntity t
            set t.usedAt = :usedAt,
                t.updatedAt = :usedAt
            where t.user.id = :userId
              and t.purpose = :purpose
              and t.usedAt is null
            """)
    int invalidateUnusedByUserIdAndPurpose(
            @Param("userId") Long userId,
            @Param("purpose") Purpose purpose,
            @Param("usedAt") LocalDateTime usedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update PasswordResetTokenJpaEntity t
            set t.usedAt = :usedAt,
                t.updatedAt = :usedAt
            where t.tokenHash = :tokenHash
              and t.purpose = :purpose
              and t.usedAt is null
              and t.expiresAt > :usedAt
            """)
    int markUsedIfUsable(
            @Param("tokenHash") String tokenHash,
            @Param("purpose") Purpose purpose,
            @Param("usedAt") LocalDateTime usedAt
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update PasswordResetTokenJpaEntity t
            set t.failedAttempts = t.failedAttempts + 1,
                t.updatedAt = :attemptedAt
            where t.id = :tokenId
              and t.usedAt is null
              and t.expiresAt > :attemptedAt
              and t.failedAttempts < :attemptLimit
            """)
    int incrementFailedAttempts(
            @Param("tokenId") Long tokenId,
            @Param("attemptedAt") LocalDateTime attemptedAt,
            @Param("attemptLimit") int attemptLimit
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update PasswordResetTokenJpaEntity t
            set t.usedAt = :usedAt,
                t.updatedAt = :usedAt
            where t.id = :tokenId
              and t.usedAt is null
              and t.failedAttempts >= :attemptLimit
            """)
    int markUsedIfAttemptLimitReached(
            @Param("tokenId") Long tokenId,
            @Param("usedAt") LocalDateTime usedAt,
            @Param("attemptLimit") int attemptLimit
    );
}
