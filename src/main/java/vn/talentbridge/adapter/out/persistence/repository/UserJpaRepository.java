package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<UserJpaEntity> findByStatus(UserStatus status, Pageable pageable);
}