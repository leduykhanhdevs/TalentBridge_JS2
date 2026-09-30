package vn.talentbridge.adapter.out.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationJpaRepository extends JpaRepository<ApplicationJpaEntity, Long>, JpaSpecificationExecutor<ApplicationJpaEntity> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ApplicationJpaEntity a where a.id = :id")
    Optional<ApplicationJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"job", "candidate", "candidate.user", "resume"})
    List<ApplicationJpaEntity> findAll(Specification<ApplicationJpaEntity> spec, Sort sort);

    @EntityGraph(attributePaths = {"job", "candidate", "candidate.user", "resume"})
    Optional<ApplicationJpaEntity> findDetailedById(Long id);
}
