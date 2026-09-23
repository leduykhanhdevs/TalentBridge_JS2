package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CreateJobCommand;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.UpdateJobCommand;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobUseCaseImpl implements JobUseCase {

    private final JobRepositoryPort jobRepository;
    private final RecruiterRepositoryPort recruiterRepository;

    public JobUseCaseImpl(JobRepositoryPort jobRepository,
                          RecruiterRepositoryPort recruiterRepository) {
        this.jobRepository = jobRepository;
        this.recruiterRepository = recruiterRepository;
    }

    @Override
    public JobDetailResult createJob(Long recruiterUserId, CreateJobCommand command) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null) {
            throw new DomainException(40301, "Bạn phải thuộc về một doanh nghiệp để đăng tin tuyển dụng");
        }

        if (recruiter.getCompany().getStatus() != CompanyStatus.APPROVED) {
            throw new DomainException(40301, "Doanh nghiệp của bạn chưa được phê duyệt để đăng tin tuyển dụng");
        }

        validateSalaryAndDeadline(command.minSalary(), command.maxSalary(), command.deadline());

        Job job = new Job();
        job.setCompanyId(recruiter.getCompany().getId());
        job.setCompanyName(recruiter.getCompany().getName());
        job.setRecruiterUserId(recruiterUserId);
        job.setTitle(command.title() != null ? command.title().trim() : "");
        job.setDescription(command.description() != null ? command.description().trim() : "");
        job.setRequirements(command.requirements());
        job.setBenefits(command.benefits());
        job.setLocation(command.location() != null && !command.location().isBlank() ? command.location().trim() : "Tại văn phòng");
        job.setCity(command.city() != null && !command.city().isBlank() ? command.city().trim() : "Chưa xác định");
        job.setAddress(command.address());
        job.setJobType(command.jobType());
        job.setExperienceLevel(command.experienceLevel());
        job.setMinSalary(command.minSalary());
        job.setMaxSalary(command.maxSalary());
        job.setIsNegotiable(Boolean.TRUE.equals(command.isNegotiable()));
        job.setDeadline(command.deadline());
        job.setStatus(JobStatus.ACTIVE);
        job.setSkills(command.skills() != null ? command.skills() : new ArrayList<>());
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);
        return JobDetailResult.from(saved);
    }

    @Override
    public JobDetailResult updateJob(Long recruiterUserId, Long jobId, UpdateJobCommand command) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng", jobId));

        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || !recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền chỉnh sửa tin tuyển dụng này");
        }

        validateSalaryAndDeadline(command.minSalary(), command.maxSalary(), command.deadline());

        job.setTitle(command.title() != null ? command.title().trim() : job.getTitle());
        job.setDescription(command.description() != null ? command.description().trim() : job.getDescription());
        job.setRequirements(command.requirements());
        job.setBenefits(command.benefits());
        if (command.location() != null && !command.location().isBlank()) {
            job.setLocation(command.location().trim());
        }
        if (command.city() != null && !command.city().isBlank()) {
            job.setCity(command.city().trim());
        }
        job.setAddress(command.address());
        job.setJobType(command.jobType());
        job.setExperienceLevel(command.experienceLevel());
        job.setMinSalary(command.minSalary());
        job.setMaxSalary(command.maxSalary());
        job.setIsNegotiable(Boolean.TRUE.equals(command.isNegotiable()));
        job.setDeadline(command.deadline());
        if (command.skills() != null) {
            job.setSkills(command.skills());
        }
        job.setUpdatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);
        return JobDetailResult.from(saved);
    }

    @Override
    public JobDetailResult closeJob(Long recruiterUserId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng", jobId));

        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || !recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền đóng tin tuyển dụng này");
        }

        job.close();
        job.setUpdatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);
        return JobDetailResult.from(saved);
    }

    @Override
    public JobDetailResult getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng", jobId));
        return JobDetailResult.from(job);
    }

    @Override
    public List<JobDetailResult> getMyJobs(Long recruiterUserId, int page, int size, JobStatus status) {
        recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        List<Job> jobs = jobRepository.findByRecruiterUserId(recruiterUserId, page, size, status);
        return jobs.stream().map(JobDetailResult::from).toList();
    }

    @Override
    public long countMyJobs(Long recruiterUserId, JobStatus status) {
        return jobRepository.countByRecruiterUserId(recruiterUserId, status);
    }

    @Override
    public List<JobDetailResult> searchJobs(String keyword, String location, String jobType, String experienceLevel,
                                           BigDecimal minSalary, BigDecimal maxSalary, int page, int size) {
        List<Job> jobs = jobRepository.search(keyword, location, jobType, experienceLevel, minSalary, maxSalary, page, size);
        return jobs.stream().map(JobDetailResult::from).toList();
    }

    @Override
    public long countSearchJobs(String keyword, String location, String jobType, String experienceLevel,
                                BigDecimal minSalary, BigDecimal maxSalary) {
        return jobRepository.countSearch(keyword, location, jobType, experienceLevel, minSalary, maxSalary);
    }

    private void validateSalaryAndDeadline(BigDecimal minSalary, BigDecimal maxSalary, LocalDate deadline) {
        if (deadline != null && !deadline.isAfter(LocalDate.now())) {
            throw new DomainException(40001, "Hạn nộp hồ sơ phải sau ngày hiện tại");
        }
        if (minSalary != null && minSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(40001, "Mức lương tối thiểu không được âm");
        }
        if (maxSalary != null && maxSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(40001, "Mức lương tối đa không được âm");
        }
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new DomainException(40001, "Mức lương tối thiểu không được lớn hơn mức lương tối đa");
        }
    }
}
