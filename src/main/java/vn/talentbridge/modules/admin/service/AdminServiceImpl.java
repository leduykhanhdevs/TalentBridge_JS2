package vn.talentbridge.modules.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.exception.AppException;
import vn.talentbridge.exception.ErrorCode;
import vn.talentbridge.modules.admin.dto.response.AdminDashboardStatsResponse;
import vn.talentbridge.modules.admin.dto.response.CompanyAdminResponse;
import vn.talentbridge.modules.admin.dto.response.JobAdminResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.company.entity.Company;
import vn.talentbridge.modules.company.enums.CompanyStatus;
import vn.talentbridge.modules.company.repository.CompanyRepository;
import vn.talentbridge.modules.job.entity.Job;
import vn.talentbridge.modules.job.enums.JobStatus;
import vn.talentbridge.modules.job.repository.JobRepository;
import vn.talentbridge.modules.user.entity.User;
import vn.talentbridge.modules.user.enums.UserStatus;
import vn.talentbridge.modules.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(int page, int size, UserStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<User> usersPage = (status != null)
                ? userRepository.findByStatus(status, pageable)
                : userRepository.findAll(pageable);

        Page<UserResponse> responsePage = usersPage.map(UserResponse::from);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CompanyAdminResponse> getAllCompanies(int page, int size, CompanyStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Company> companyPage = (status != null)
                ? companyRepository.findByStatus(status, pageable)
                : companyRepository.findAll(pageable);

        Page<CompanyAdminResponse> responsePage = companyPage.map(CompanyAdminResponse::from);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public CompanyAdminResponse updateCompanyStatus(Long companyId, CompanyStatus status) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setStatus(status);
        Company updatedCompany = companyRepository.save(company);
        return CompanyAdminResponse.from(updatedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobAdminResponse> getAllJobs(int page, int size, JobStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = (status != null)
                ? jobRepository.findByStatus(status, pageable)
                : jobRepository.findAll(pageable);

        Page<JobAdminResponse> responsePage = jobPage.map(JobAdminResponse::from);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public JobAdminResponse updateJobStatus(Long jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        job.setStatus(status);
        Job updatedJob = jobRepository.save(job);
        return JobAdminResponse.from(updatedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        return AdminDashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalCompanies(companyRepository.count())
                .pendingCompanies(companyRepository.countByStatus(CompanyStatus.PENDING))
                .totalJobs(jobRepository.count())
                .activeJobs(jobRepository.countByStatus(JobStatus.ACTIVE))
                .pendingJobs(jobRepository.countByStatus(JobStatus.PENDING))
                .build();
    }
}