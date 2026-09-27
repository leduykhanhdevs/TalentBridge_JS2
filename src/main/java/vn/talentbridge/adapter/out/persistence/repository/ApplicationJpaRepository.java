package vn.talentbridge.adapter.out.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;

import java.util.Optional;

@Repository
public interface ApplicationJpaRepository extends JpaRepository<ApplicationJpaEntity, Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ApplicationJpaEntity a where a.id = :id")
    Optional<ApplicationJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
