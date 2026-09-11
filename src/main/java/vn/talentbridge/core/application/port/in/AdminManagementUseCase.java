package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;

public interface AdminManagementUseCase {
    List<UserResult> getAllUsers(int page, int size);
    long countUsers();
    UserResult updateUserStatus(Long userId, UserStatus status);

    List<CompanyResult> getAllCompanies(int page, int size, CompanyStatus status);
    long countCompanies();
    CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status);

    List<JobResult> getAllJobs(int page, int size, JobStatus status);
    long countJobs();
    JobResult updateJobStatus(Long jobId, JobStatus status);

    AdminDashboardStatsResult getDashboardStats();
}