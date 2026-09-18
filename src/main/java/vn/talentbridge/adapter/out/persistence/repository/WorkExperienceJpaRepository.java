package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.WorkExperienceJpaEntity;

import java.util.List;

@Repository
public interface WorkExperienceJpaRepository extends JpaRepository<WorkExperienceJpaEntity, Long> {
    List<WorkExperienceJpaEntity> findByCandidateIdOrderByStartDateDesc(Long candidateId);
}
