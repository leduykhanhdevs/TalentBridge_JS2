package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.JobStatusHistoryJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.JobStatusHistoryJpaRepository;
import vn.talentbridge.core.application.port.out.JobStatusHistoryRepositoryPort;
import vn.talentbridge.core.domain.model.JobStatusHistory;

import java.util.List;

@Component
@Transactional
public class JobStatusHistoryRepositoryAdapter implements JobStatusHistoryRepositoryPort {
    private final JobStatusHistoryJpaRepository repository;

    public JobStatusHistoryRepositoryAdapter(JobStatusHistoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public JobStatusHistory save(JobStatusHistory history) {
        JobStatusHistoryJpaEntity entity = new JobStatusHistoryJpaEntity();
        entity.setJobId(history.getJobId());
        entity.setFromStatus(history.getFromStatus());
        entity.setToStatus(history.getToStatus());
        entity.setReason(history.getReason());
        entity.setChangedByUserId(history.getChangedByUserId());
        entity.setChangedAt(history.getChangedAt());
        JobStatusHistoryJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobStatusHistory> findByJobId(Long jobId) {
        return repository.findByJobIdOrderByChangedAtDesc(jobId).stream().map(this::toDomain).toList();
    }

    private JobStatusHistory toDomain(JobStatusHistoryJpaEntity entity) {
        return new JobStatusHistory(entity.getId(), entity.getJobId(), entity.getFromStatus(),
                entity.getToStatus(), entity.getReason(), entity.getChangedByUserId(), entity.getChangedAt());
    }
}
