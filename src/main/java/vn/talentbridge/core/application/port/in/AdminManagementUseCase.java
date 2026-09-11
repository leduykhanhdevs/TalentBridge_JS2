package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.*;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;

public interface AdminManagementUseCase {
    List<UserResult> getAllUsers(int page, int size);
    long countUsers();
    UserResult updateUserStatus(Long userId, UserStatus status);

    List<CompanyResult> getAllCompanies(int page, int size, String keyword, CompanyStatus status);
    long countCompanies(String keyword, CompanyStatus status);
    CompanyResult getCompanyById(Long id);
    CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status, String reason);

    List<JobResult> getAllJobs(int page, int size, JobStatus status);
    long countJobs();
    JobResult updateJobStatus(Long jobId, JobStatus status);

    List<RecruiterResult> getAllRecruiters(int page, int size, String keyword);
    long countRecruiters(String keyword);
    RecruiterResult getRecruiterById(Long id);

    List<CandidateResult> getAllCandidates(int page, int size, String keyword, UserStatus status);
    long countCandidates(String keyword, UserStatus status);
    CandidateResult getCandidateById(Long id);

    AdminDashboardStatsResult getDashboardStats();
}