package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;

import java.util.List;

@Repository
public interface ApplicationJpaRepository extends JpaRepository<ApplicationJpaEntity, Long> {

    @EntityGraph(attributePaths = {"job", "candidate", "candidate.user"})
    List<ApplicationJpaEntity> findByJobId(Long jobId);
}
