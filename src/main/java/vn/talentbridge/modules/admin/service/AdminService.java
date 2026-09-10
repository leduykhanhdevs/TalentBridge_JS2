package vn.talentbridge.modules.admin.service;

import vn.talentbridge.common.PageResponse;
import vn.talentbridge.modules.admin.dto.response.AdminDashboardStatsResponse;
import vn.talentbridge.modules.admin.dto.response.CompanyAdminResponse;
import vn.talentbridge.modules.admin.dto.response.JobAdminResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.company.enums.CompanyStatus;
import vn.talentbridge.modules.job.enums.JobStatus;
import vn.talentbridge.modules.user.enums.UserStatus;

public interface AdminService {
    PageResponse<UserResponse> getAllUsers(int page, int size, UserStatus status);
    UserResponse updateUserStatus(Long userId, UserStatus status);

    PageResponse<CompanyAdminResponse> getAllCompanies(int page, int size, CompanyStatus status);
    CompanyAdminResponse updateCompanyStatus(Long companyId, CompanyStatus status);

    PageResponse<JobAdminResponse> getAllJobs(int page, int size, JobStatus status);
    JobAdminResponse updateJobStatus(Long jobId, JobStatus status);

    AdminDashboardStatsResponse getDashboardStats();
}