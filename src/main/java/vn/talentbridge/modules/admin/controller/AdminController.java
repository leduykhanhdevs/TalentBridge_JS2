package vn.talentbridge.modules.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.modules.admin.dto.request.UpdateCompanyStatusRequest;
import vn.talentbridge.modules.admin.dto.request.UpdateJobStatusRequest;
import vn.talentbridge.modules.admin.dto.request.UpdateUserStatusRequest;
import vn.talentbridge.modules.admin.dto.response.AdminDashboardStatsResponse;
import vn.talentbridge.modules.admin.dto.response.CompanyAdminResponse;
import vn.talentbridge.modules.admin.dto.response.JobAdminResponse;
import vn.talentbridge.modules.admin.service.AdminService;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.company.enums.CompanyStatus;
import vn.talentbridge.modules.job.enums.JobStatus;
import vn.talentbridge.modules.user.enums.UserStatus;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin Management", description = "Quản trị hệ thống: Quản lý người dùng, duyệt doanh nghiệp & kiểm duyệt tin tuyển dụng")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Danh sách người dùng", description = "Lấy danh sách người dùng có phân trang và lọc theo trạng thái (Yêu cầu ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserStatus status
    ) {
        PageResponse<UserResponse> users = adminService.getAllUsers(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Khóa hoặc Mở khóa tài khoản", description = "Cập nhật trạng thái người dùng sang ACTIVE hoặc LOCKED")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResponse updatedUser = adminService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", updatedUser));
    }

    @GetMapping("/companies")
    @Operation(summary = "Danh sách doanh nghiệp", description = "Lấy danh sách công ty có phân trang và lọc theo trạng thái duyệt PENDING/APPROVED/REJECTED")
    public ResponseEntity<ApiResponse<PageResponse<CompanyAdminResponse>>> getAllCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CompanyStatus status
    ) {
        PageResponse<CompanyAdminResponse> companies = adminService.getAllCompanies(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @PatchMapping("/companies/{id}/status")
    @Operation(summary = "Phê duyệt hoặc Từ chối doanh nghiệp", description = "Duyệt công ty mới đăng ký sang APPROVED hoặc REJECTED")
    public ResponseEntity<ApiResponse<CompanyAdminResponse>> updateCompanyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyStatusRequest request
    ) {
        CompanyAdminResponse updatedCompany = adminService.updateCompanyStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái doanh nghiệp thành công", updatedCompany));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Danh sách tin tuyển dụng", description = "Lấy danh sách việc làm có phân trang và lọc theo trạng thái")
    public ResponseEntity<ApiResponse<PageResponse<JobAdminResponse>>> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status
    ) {
        PageResponse<JobAdminResponse> jobs = adminService.getAllJobs(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @PatchMapping("/jobs/{id}/status")
    @Operation(summary = "Kiểm duyệt tin tuyển dụng", description = "Phê duyệt, đóng hoặc gỡ tin vi phạm")
    public ResponseEntity<ApiResponse<JobAdminResponse>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request
    ) {
        JobAdminResponse updatedJob = adminService.updateJobStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Kiểm duyệt tin tuyển dụng thành công", updatedJob));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Thống kê tổng quan Admin Dashboard", description = "Lấy tổng số user, công ty chờ duyệt, tin tuyển dụng đang hoạt động")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}