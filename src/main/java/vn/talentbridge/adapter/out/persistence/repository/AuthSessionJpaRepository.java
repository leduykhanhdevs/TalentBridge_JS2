package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}