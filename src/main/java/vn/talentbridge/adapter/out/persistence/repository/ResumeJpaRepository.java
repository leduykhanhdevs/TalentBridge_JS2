package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ResumeJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeJpaRepository extends JpaRepository<ResumeJpaEntity, Long> {

    List<ResumeJpaEntity> findByCandidateIdOrderByIsDefaultDescCreatedAtDesc(Long candidateId);

    Optional<ResumeJpaEntity> findByCandidateIdAndIsDefaultTrue(Long candidateId);

    @Modifying
    @Query("UPDATE ResumeJpaEntity r SET r.isDefault = false WHERE r.candidate.id = :candidateId")
    void clearDefaultByCandidateId(@Param("candidateId") Long candidateId);
}
