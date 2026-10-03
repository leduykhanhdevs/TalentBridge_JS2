package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.CreateJobCommand;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.MyJobStatsResult;
import vn.talentbridge.core.application.dto.UpdateJobCommand;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.util.List;

public class TransactionalJobUseCase implements JobUseCase {
    private final JobUseCase delegate;

    public TransactionalJobUseCase(JobUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobDetailResult createJob(Long recruiterUserId, CreateJobCommand command) {
        return delegate.createJob(recruiterUserId, command);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobDetailResult updateJob(Long recruiterUserId, Long jobId, UpdateJobCommand command) {
        return delegate.updateJob(recruiterUserId, jobId, command);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JobDetailResult closeJob(Long recruiterUserId, Long jobId) {
        return delegate.closeJob(recruiterUserId, jobId);
    }

    @Override public JobDetailResult getJobById(Long jobId) { return delegate.getJobById(jobId); }
    @Override public List<JobDetailResult> getMyJobs(Long userId, int page, int size, JobStatus status) { return delegate.getMyJobs(userId, page, size, status); }
    @Override public long countMyJobs(Long userId, JobStatus status) { return delegate.countMyJobs(userId, status); }
    @Override public MyJobStatsResult getMyJobStats(Long userId) { return delegate.getMyJobStats(userId); }
    @Override public List<JobDetailResult> searchJobs(String keyword, String location, String jobType, String experienceLevel,
                                                       BigDecimal minSalary, BigDecimal maxSalary, int page, int size,
                                                       String sortBy, String sortDirection) {
        return delegate.searchJobs(keyword, location, jobType, experienceLevel, minSalary, maxSalary,
                page, size, sortBy, sortDirection);
    }
    @Override public long countSearchJobs(String keyword, String location, String jobType, String experienceLevel,
                                          BigDecimal minSalary, BigDecimal maxSalary) {
        return delegate.countSearchJobs(keyword, location, jobType, experienceLevel, minSalary, maxSalary);
    }
}
