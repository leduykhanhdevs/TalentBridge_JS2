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
import vn.talentbridge.adapter.in.web.dto.response.CompanyAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.JobAdminResponse;
import vn.talentbridge.adapter.in.web.dto.response.UserResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.JobResult;
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
@Tag(name = "Admin Management", description = "Quáº£n trá»‹ há»‡ thá»‘ng: Quáº£n lÃ½ ngÆ°á»i dÃ¹ng, duyá»‡t doanh nghiá»‡p & kiá»ƒm duyá»‡t tin tuyá»ƒn dá»¥ng")
public class AdminController {

    private final AdminManagementUseCase adminManagementUseCase;

    @GetMapping("/users")
    @Operation(summary = "Danh sÃ¡ch ngÆ°á»i dÃ¹ng", description = "Láº¥y danh sÃ¡ch ngÆ°á»i dÃ¹ng cÃ³ phÃ¢n trang vÃ  lá»c theo tráº¡ng thÃ¡i (YÃªu cáº§u ROLE_ADMIN)")
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
    @Operation(summary = "KhÃ³a hoáº·c Má»Ÿ khÃ³a tÃ i khoáº£n", description = "Cáº­p nháº­t tráº¡ng thÃ¡i ngÆ°á»i dÃ¹ng")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResult updatedUser = adminManagementUseCase.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cáº­p nháº­t tráº¡ng thÃ¡i tÃ i khoáº£n thÃ nh cÃ´ng", UserResponse.from(updatedUser)));
    }

    @GetMapping("/companies")
    @Operation(summary = "Danh sÃ¡ch doanh nghiá»‡p", description = "Láº¥y danh sÃ¡ch cÃ´ng ty cÃ³ phÃ¢n trang vÃ  lá»c theo tráº¡ng thÃ¡i duyá»‡t")
    public ResponseEntity<ApiResponse<PageResponse<CompanyAdminResponse>>> getAllCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CompanyStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        List<CompanyResult> companies = adminManagementUseCase.getAllCompanies(pageIndex, size, status);
        long totalElements = adminManagementUseCase.countCompanies();
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

    @PatchMapping("/companies/{id}/status")
    @Operation(summary = "PhÃª duyá»‡t hoáº·c Tá»« chá»‘i doanh nghiá»‡p", description = "Duyá»‡t cÃ´ng ty má»›i Ä‘Äƒng kÃ½ sang APPROVED hoáº·c REJECTED")
    public ResponseEntity<ApiResponse<CompanyAdminResponse>> updateCompanyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyStatusRequest request
    ) {
        CompanyResult updatedCompany = adminManagementUseCase.updateCompanyStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cáº­p nháº­t tráº¡ng thÃ¡i doanh nghiá»‡p thÃ nh cÃ´ng", CompanyAdminResponse.from(updatedCompany)));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Danh sÃ¡ch tin tuyá»ƒn dá»¥ng", description = "Láº¥y danh sÃ¡ch viá»‡c lÃ m cÃ³ phÃ¢n trang vÃ  lá»c theo tráº¡ng thÃ¡i")
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
    @Operation(summary = "Kiá»ƒm duyá»‡t tin tuyá»ƒn dá»¥ng", description = "PhÃª duyá»‡t, Ä‘Ã³ng hoáº·c gá»¡ tin vi pháº¡m")
    public ResponseEntity<ApiResponse<JobAdminResponse>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request
    ) {
        JobResult updatedJob = adminManagementUseCase.updateJobStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Kiá»ƒm duyá»‡t tin tuyá»ƒn dá»¥ng thÃ nh cÃ´ng", JobAdminResponse.from(updatedJob)));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Thá»‘ng kÃª tá»•ng quan Admin Dashboard", description = "Láº¥y tá»•ng sá»‘ user, cÃ´ng ty chá» duyá»‡t, tin tuyá»ƒn dá»¥ng Ä‘ang hoáº¡t Ä‘á»™ng")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResult stats = adminManagementUseCase.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(AdminDashboardStatsResponse.from(stats)));
    }
}