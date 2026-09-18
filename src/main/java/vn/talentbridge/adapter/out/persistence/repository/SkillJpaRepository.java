package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.SkillJpaEntity;

import java.util.Optional;

@Repository
public interface SkillJpaRepository extends JpaRepository<SkillJpaEntity, Integer> {
    Optional<SkillJpaEntity> findByNameIgnoreCase(String name);
}
