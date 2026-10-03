package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.JobStatusHistoryJpaEntity;

import java.util.List;

@Repository
public interface JobStatusHistoryJpaRepository extends JpaRepository<JobStatusHistoryJpaEntity, Long> {
    List<JobStatusHistoryJpaEntity> findByJobIdOrderByChangedAtDesc(Long jobId);
}
