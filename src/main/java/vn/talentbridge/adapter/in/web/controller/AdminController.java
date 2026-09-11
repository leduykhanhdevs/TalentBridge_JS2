package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.UpdateCompanyStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateJobStatusRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateUserStatusRequest;
import vn.talentbridge.adapter.in.web.dto.response.AdminDashboardStatsResponse;
import vn.talentbridge.adapter.in.web.dto.response.CandidateAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.CompanyAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.JobAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.RecruiterAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.UserResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;
import vn.talentbridge.core.application.dto.CandidateResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.AdminManagementUseCase;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin Management", description = "Quản trị hệ thống: Quản lý người dùng, ứng viên, nhà tuyển dụng, duyệt doanh nghiệp & kiểm duyệt tin tuyển dụng")
public class AdminController {

    private final AdminManagementUseCase adminManagementUseCase;

    @GetMapping("/users")
    @Operation(summary = "Danh sách người dùng", description = "Lấy danh sách người dùng có phân trang và lọc theo trạng thái (Yêu cầu ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<UserResult> users = adminManagementUseCase.getAllUsers(pageIndex, size);
        long totalElements = adminManagementUseCase.countUsers();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<UserResponse> content = users.stream().map(UserResponse::from).toList();
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Khóa hoặc Mở khóa tài khoản", description = "Cập nhật trạng thái người dùng")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResult updatedUser = adminManagementUseCase.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", UserResponse.from(updatedUser)));
    }

    @GetMapping("/companies")
    @Operation(summary = "Danh sách doanh nghiệp", description = "Lấy danh sách công ty có phân trang, tìm kiếm theo từ khóa (tên, MST, địa chỉ) và lọc theo trạng thái duyệt")
    public ResponseEntity<ApiResponse<PageResponse<CompanyAdminResponse>>> getAllCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CompanyStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<CompanyResult> companies = adminManagementUseCase.getAllCompanies(pageIndex, size, keyword, status);
        long totalElements = adminManagementUseCase.countCompanies(keyword, status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<CompanyAdminResponse> content = companies.stream().map(CompanyAdminResponse::from).toList();
        PageResponse<CompanyAdminResponse> pageResponse = PageResponse.<CompanyAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Chi tiết doanh nghiệp", description = "Xem chi tiết thông tin doanh nghiệp và người gửi yêu cầu phê duyệt")
    public ResponseEntity<ApiResponse<CompanyAdminResponse>> getCompanyById(@PathVariable Long id) {
        CompanyResult company = adminManagementUseCase.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(CompanyAdminResponse.from(company)));
    }

    @PatchMapping("/companies/{id}/status")
    @Operation(summary = "Phê duyệt hoặc Từ chối doanh nghiệp", description = "Duyệt công ty mới đăng ký sang APPROVED hoặc REJECTED kèm lý do")
    public ResponseEntity<ApiResponse<CompanyAdminResponse>> updateCompanyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyStatusRequest request
    ) {
        CompanyResult updatedCompany = adminManagementUseCase.updateCompanyStatus(id, request.getStatus(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái doanh nghiệp thành công", CompanyAdminResponse.from(updatedCompany)));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Danh sách tin tuyển dụng", description = "Lấy danh sách việc làm có phân trang và lọc theo trạng thái")
    public ResponseEntity<ApiResponse<PageResponse<JobAdminResponse>>> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<JobResult> jobs = adminManagementUseCase.getAllJobs(pageIndex, size, status);
        long totalElements = adminManagementUseCase.countJobs();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<JobAdminResponse> content = jobs.stream().map(JobAdminResponse::from).toList();
        PageResponse<JobAdminResponse> pageResponse = PageResponse.<JobAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PatchMapping("/jobs/{id}/status")
    @Operation(summary = "Kiểm duyệt tin tuyển dụng", description = "Phê duyệt, đóng hoặc gỡ tin vi phạm")
    public ResponseEntity<ApiResponse<JobAdminResponse>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request
    ) {
        JobResult updatedJob = adminManagementUseCase.updateJobStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Kiểm duyệt tin tuyển dụng thành công", JobAdminResponse.from(updatedJob)));
    }

    @GetMapping("/recruiters")
    @Operation(summary = "Danh sách nhà tuyển dụng", description = "Lấy danh sách nhà tuyển dụng (HR) có phân trang và tìm kiếm theo từ khóa (tên, email, tên công ty)")
    public ResponseEntity<ApiResponse<PageResponse<RecruiterAdminResponse>>> getAllRecruiters(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<RecruiterResult> recruiters = adminManagementUseCase.getAllRecruiters(pageIndex, size, keyword);
        long totalElements = adminManagementUseCase.countRecruiters(keyword);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<RecruiterAdminResponse> content = recruiters.stream().map(RecruiterAdminResponse::from).toList();
        PageResponse<RecruiterAdminResponse> pageResponse = PageResponse.<RecruiterAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/recruiters/{id}")
    @Operation(summary = "Chi tiết nhà tuyển dụng", description = "Xem thông tin chi tiết của một nhà tuyển dụng")
    public ResponseEntity<ApiResponse<RecruiterAdminResponse>> getRecruiterById(@PathVariable Long id) {
        RecruiterResult recruiter = adminManagementUseCase.getRecruiterById(id);
        return ResponseEntity.ok(ApiResponse.success(RecruiterAdminResponse.from(recruiter)));
    }

    @GetMapping("/candidates")
    @Operation(summary = "Danh sách ứng viên", description = "Lấy danh sách ứng viên (Candidate) có phân trang, tìm kiếm theo từ khóa (tên, email, title, city) và lọc theo trạng thái tài khoản")
    public ResponseEntity<ApiResponse<PageResponse<CandidateAdminResponse>>> getAllCandidates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<CandidateResult> candidates = adminManagementUseCase.getAllCandidates(pageIndex, size, keyword, status);
        long totalElements = adminManagementUseCase.countCandidates(keyword, status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<CandidateAdminResponse> content = candidates.stream().map(CandidateAdminResponse::from).toList();
        PageResponse<CandidateAdminResponse> pageResponse = PageResponse.<CandidateAdminResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/candidates/{id}")
    @Operation(summary = "Chi tiết ứng viên", description = "Xem thông tin chi tiết hồ sơ của một ứng viên")
    public ResponseEntity<ApiResponse<CandidateAdminResponse>> getCandidateById(@PathVariable Long id) {
        CandidateResult candidate = adminManagementUseCase.getCandidateById(id);
        return ResponseEntity.ok(ApiResponse.success(CandidateAdminResponse.from(candidate)));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Thống kê tổng quan Admin Dashboard", description = "Lấy tổng số user, công ty chờ duyệt, tin tuyển dụng đang hoạt động")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResult stats = adminManagementUseCase.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(AdminDashboardStatsResponse.from(stats)));
    }
}