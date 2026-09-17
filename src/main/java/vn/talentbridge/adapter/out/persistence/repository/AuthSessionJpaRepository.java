package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.AuthSessionJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthSessionJpaRepository
        extends JpaRepository<AuthSessionJpaEntity, Long> {

    Optional<AuthSessionJpaEntity> findBySessionId(String sessionId);

    Optional<AuthSessionJpaEntity>
    findBySessionIdAndRevokedAtIsNullAndExpiresAtAfter(
            String sessionId,
            LocalDateTime now
    );

    List<AuthSessionJpaEntity> findAllByUser_Id(Long userId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update AuthSessionJpaEntity s
            set s.refreshTokenHash = :newHash,
                s.updatedAt = :now
            where s.sessionId = :sessionId
              and s.user.id = :userId
              and s.refreshTokenHash = :expectedHash
              and s.revokedAt is null
              and s.expiresAt > :now
            """)
    int rotateRefreshTokenIfActive(
            @Param("sessionId") String sessionId,
            @Param("userId") Long userId,
            @Param("expectedHash") String expectedHash,
            @Param("newHash") String newHash,
            @Param("now") LocalDateTime now
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update AuthSessionJpaEntity s
            set s.revokedAt = :now,
                s.updatedAt = :now
            where s.sessionId = :sessionId
              and s.user.id = :userId
              and s.revokedAt is null
              and s.expiresAt > :now
            """)
    int revokeActiveSession(
            @Param("sessionId") String sessionId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update AuthSessionJpaEntity s
        set s.revokedAt = :now,
            s.updatedAt = :now
        where s.user.id = :userId
          and s.revokedAt is null
          and s.expiresAt > :now
        """)
    int revokeAllActiveSessions(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

}