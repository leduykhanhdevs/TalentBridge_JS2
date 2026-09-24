package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JobRepositoryPort {
    Optional<Job> findById(Long id);
    Job save(Job job);
    List<Job> findAll(int page, int size, JobStatus status);
    List<Job> findByRecruiterUserId(Long recruiterUserId, int page, int size, JobStatus status);
    long countByRecruiterUserId(Long recruiterUserId, JobStatus status);
    List<Job> findByCompanyId(Long companyId, int page, int size, JobStatus status);
    long countByCompanyId(Long companyId, JobStatus status);
    List<Job> search(String keyword, String location, String jobType, String experienceLevel, BigDecimal minSalary, BigDecimal maxSalary, int page, int size);
    long countSearch(String keyword, String location, String jobType, String experienceLevel, BigDecimal minSalary, BigDecimal maxSalary);
    long count();
    long countByStatus(JobStatus status);
}