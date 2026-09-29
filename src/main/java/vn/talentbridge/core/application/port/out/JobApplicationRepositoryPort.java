package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.JobApplication;

import java.util.List;

public interface JobApplicationRepositoryPort {
    List<JobApplication> findByJobId(Long jobId);
}
