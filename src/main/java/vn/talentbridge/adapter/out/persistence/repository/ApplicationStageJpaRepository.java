package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationStageJpaEntity;

import java.util.List;

@Repository
public interface ApplicationStageJpaRepository extends JpaRepository<ApplicationStageJpaEntity, Long> {

    @EntityGraph(attributePaths = {"changedByUser"})
    List<ApplicationStageJpaEntity> findByApplicationIdOrderByChangedAtDesc(Long applicationId);
}
