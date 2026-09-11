package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobRepositoryPort {
    Optional<Job> findById(Long id);
    Job save(Job job);
    List<Job> findAll(int page, int size, JobStatus status);
    long count();
    long countByStatus(JobStatus status);
}