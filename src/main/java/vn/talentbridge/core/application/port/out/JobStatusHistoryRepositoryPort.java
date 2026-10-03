package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.JobStatusHistory;

import java.util.List;

public interface JobStatusHistoryRepositoryPort {
    JobStatusHistory save(JobStatusHistory history);
    List<JobStatusHistory> findByJobId(Long jobId);
}
