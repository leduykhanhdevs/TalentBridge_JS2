package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.AdminManagementUseCase;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;
import java.util.stream.Collectors;

public class AdminManagementUseCaseImpl implements AdminManagementUseCase {
    private final UserRepositoryPort userRepository;
    private final CompanyRepositoryPort companyRepository;
    private final JobRepositoryPort jobRepository;
    private final RecruiterRepositoryPort recruiterRepository;

    public AdminManagementUseCaseImpl(UserRepositoryPort userRepository,
                                      CompanyRepositoryPort companyRepository,
                                      JobRepositoryPort jobRepository,
                                      RecruiterRepositoryPort recruiterRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.recruiterRepository = recruiterRepository;
    }

    @Override
    public List<UserResult> getAllUsers(int page, int size) {
        return userRepository.findAll(page, size).stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public UserResult updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
        user.setStatus(status);
        User updated = userRepository.save(user);
        return UserResult.from(updated);
    }

    @Override
    public List<CompanyResult> getAllCompanies(int page, int size, CompanyStatus status) {
        return companyRepository.findAll(page, size, status).stream()
                .map(CompanyResult::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countCompanies() {
        return companyRepository.count();
    }

    @Override
    public CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Doanh nghiệp", companyId));
        company.setStatus(status);
        Company updated = companyRepository.save(company);
        return CompanyResult.from(updated);
    }

    @Override
    public List<JobResult> getAllJobs(int page, int size, JobStatus status) {
        return jobRepository.findAll(page, size, status).stream()
                .map(JobResult::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countJobs() {
        return jobRepository.count();
    }

    @Override
    public JobResult updateJobStatus(Long jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        job.setStatus(status);
        Job updated = jobRepository.save(job);
        return JobResult.from(updated);
    }

    @Override
    public List<RecruiterResult> getAllRecruiters(int page, int size, String keyword) {
        return recruiterRepository.findAll(page, size, keyword).stream()
                .map(RecruiterResult::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countRecruiters(String keyword) {
        return recruiterRepository.count(keyword);
    }

    @Override
    public RecruiterResult getRecruiterById(Long id) {
        Recruiter recruiter = recruiterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhà tuyển dụng", id));
        return RecruiterResult.from(recruiter);
    }

    @Override
    public AdminDashboardStatsResult getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCompanies = companyRepository.count();
        long pendingCompanies = companyRepository.countByStatus(CompanyStatus.PENDING);
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByStatus(JobStatus.ACTIVE);
        long pendingJobs = jobRepository.countByStatus(JobStatus.PENDING);

        return new AdminDashboardStatsResult(
                totalUsers,
                totalCompanies,
                pendingCompanies,
                totalJobs,
                activeJobs,
                pendingJobs
        );
    }
}