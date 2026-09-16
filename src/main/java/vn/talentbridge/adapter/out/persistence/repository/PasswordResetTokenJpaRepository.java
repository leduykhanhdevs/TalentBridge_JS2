package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.PasswordResetTokenJpaEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenJpaRepository
        extends JpaRepository<PasswordResetTokenJpaEntity, Long> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

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
            where t.tokenHash = :tokenHash
              and t.usedAt is null
              and t.expiresAt > :usedAt
            """)
    int markUsedIfUsable(
            @Param("tokenHash") String tokenHash,
            @Param("usedAt") LocalDateTime usedAt
    );
}