package vn.talentbridge.modules.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.AdminManagementUseCase;
import vn.talentbridge.modules.admin.dto.response.AdminDashboardStatsResponse;
import vn.talentbridge.modules.admin.dto.response.CompanyAdminResponse;
import vn.talentbridge.modules.admin.dto.response.JobAdminResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.company.enums.CompanyStatus;
import vn.talentbridge.modules.job.enums.JobStatus;
import vn.talentbridge.modules.user.enums.UserStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminManagementUseCase adminManagementUseCase;

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size, UserStatus status) {
        int pageIndex = Math.max(0, page - 1);
        List<UserResult> users = adminManagementUseCase.getAllUsers(pageIndex, size);
        long totalElements = adminManagementUseCase.countUsers();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<UserResponse> content = users.stream().map(this::toUserResponse).toList();
        return PageResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();
    }

    @Override
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        vn.talentbridge.core.domain.vo.UserStatus domainStatus =
                vn.talentbridge.core.domain.vo.UserStatus.valueOf(status.name());
        UserResult updatedUser = adminManagementUseCase.updateUserStatus(userId, domainStatus);
        return toUserResponse(updatedUser);
    }

    @Override
    public PageResponse<CompanyAdminResponse> getAllCompanies(int page, int size, CompanyStatus status) {
        int pageIndex = Math.max(0, page - 1);
        vn.talentbridge.core.domain.vo.CompanyStatus domainStatus =
                status != null ? vn.talentbridge.core.domain.vo.CompanyStatus.valueOf(status.name()) : null;

        List<CompanyResult> companies = adminManagementUseCase.getAllCompanies(pageIndex, size, domainStatus);
        long totalElements = adminManagementUseCase.countCompanies();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<CompanyAdminResponse> content = companies.stream().map(this::toCompanyResponse).toList();
        return PageResponse.<CompanyAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();
    }

    @Override
    public CompanyAdminResponse updateCompanyStatus(Long companyId, CompanyStatus status) {
        vn.talentbridge.core.domain.vo.CompanyStatus domainStatus =
                vn.talentbridge.core.domain.vo.CompanyStatus.valueOf(status.name());
        CompanyResult updatedCompany = adminManagementUseCase.updateCompanyStatus(companyId, domainStatus);
        return toCompanyResponse(updatedCompany);
    }

    @Override
    public PageResponse<JobAdminResponse> getAllJobs(int page, int size, JobStatus status) {
        int pageIndex = Math.max(0, page - 1);
        vn.talentbridge.core.domain.vo.JobStatus domainStatus =
                status != null ? vn.talentbridge.core.domain.vo.JobStatus.valueOf(status.name()) : null;

        List<JobResult> jobs = adminManagementUseCase.getAllJobs(pageIndex, size, domainStatus);
        long totalElements = adminManagementUseCase.countJobs();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<JobAdminResponse> content = jobs.stream().map(this::toJobResponse).toList();
        return PageResponse.<JobAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();
    }

    @Override
    public JobAdminResponse updateJobStatus(Long jobId, JobStatus status) {
        vn.talentbridge.core.domain.vo.JobStatus domainStatus =
                vn.talentbridge.core.domain.vo.JobStatus.valueOf(status.name());
        JobResult updatedJob = adminManagementUseCase.updateJobStatus(jobId, domainStatus);
        return toJobResponse(updatedJob);
    }

    @Override
    public AdminDashboardStatsResponse getDashboardStats() {
        AdminDashboardStatsResult stats = adminManagementUseCase.getDashboardStats();
        return AdminDashboardStatsResponse.builder()
                .totalUsers(stats.totalUsers())
                .totalCompanies(stats.totalCompanies())
                .pendingCompanies(stats.pendingCompanies())
                .totalJobs(stats.totalJobs())
                .activeJobs(stats.activeJobs())
                .pendingJobs(stats.pendingJobs())
                .build();
    }

    private UserResponse toUserResponse(UserResult user) {
        return UserResponse.builder()
                .id(user.id())
                .email(user.email())
                .fullName(user.fullName())
                .phone(user.phoneNumber())
                .avatarUrl(user.avatarUrl())
                .status(user.status() != null ? UserStatus.valueOf(user.status()) : null)
                .roles(user.roles())
                .createdAt(user.createdAt())
                .build();
    }

    private CompanyAdminResponse toCompanyResponse(CompanyResult company) {
        return CompanyAdminResponse.builder()
                .id(company.id())
                .name(company.name())
                .logoUrl(company.logoUrl())
                .website(company.website())
                .description(company.description())
                .companySize(company.companySize())
                .address(company.address())
                .status(company.status() != null ? CompanyStatus.valueOf(company.status()) : null)
                .createdAt(company.createdAt())
                .build();
    }

    private JobAdminResponse toJobResponse(JobResult job) {
        return JobAdminResponse.builder()
                .id(job.id())
                .companyId(job.companyId())
                .companyName(job.companyName())
                .title(job.title())
                .city(job.location())
                .jobType(job.jobType())
                .experienceLevel(job.experienceLevel())
                .salaryMin(job.minSalary())
                .salaryMax(job.maxSalary())
                .deadline(job.deadline())
                .status(job.status() != null ? JobStatus.valueOf(job.status()) : null)
                .createdAt(job.createdAt())
                .build();
    }
}