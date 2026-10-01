package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CreateJobCommand;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.UpdateJobCommand;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.util.List;

public interface JobUseCase {

    JobDetailResult createJob(Long recruiterUserId, CreateJobCommand command);

    JobDetailResult updateJob(Long recruiterUserId, Long jobId, UpdateJobCommand command);

    JobDetailResult closeJob(Long recruiterUserId, Long jobId);

    JobDetailResult getJobById(Long jobId);

    List<JobDetailResult> getMyJobs(Long recruiterUserId, int page, int size, JobStatus status);

    long countMyJobs(Long recruiterUserId, JobStatus status);

    default List<JobDetailResult> searchJobs(String keyword, String location, String jobType, String experienceLevel,
                                             BigDecimal minSalary, BigDecimal maxSalary, int page, int size) {
        return searchJobs(keyword, location, jobType, experienceLevel, minSalary, maxSalary, page, size, "createdAt", "desc");
    }

    List<JobDetailResult> searchJobs(String keyword, String location, String jobType, String experienceLevel,
                                     BigDecimal minSalary, BigDecimal maxSalary, int page, int size,
                                     String sortBy, String sortDirection);

    long countSearchJobs(String keyword, String location, String jobType, String experienceLevel,
                         BigDecimal minSalary, BigDecimal maxSalary);
}
