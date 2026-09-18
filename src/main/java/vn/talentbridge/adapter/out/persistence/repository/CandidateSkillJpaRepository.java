package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CandidateSkillJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateSkillJpaRepository extends JpaRepository<CandidateSkillJpaEntity, Long> {
    List<CandidateSkillJpaEntity> findByCandidateIdOrderByIdAsc(Long candidateId);
    Optional<CandidateSkillJpaEntity> findByCandidateIdAndSkillId(Long candidateId, Integer skillId);
}
