package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.CreateJobRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateJobRequest;
import vn.talentbridge.adapter.in.web.dto.response.JobResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.CreateJobCommand;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.UpdateJobCommand;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management & Search", description = "API quản lý và tìm kiếm tin tuyển dụng")
public class JobController {

    private final JobUseCase jobUseCase;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "HR đăng tin tuyển dụng mới", description = "Tạo mới tin tuyển dụng thuộc về doanh nghiệp đã được phê duyệt của HR")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateJobRequest request) {

        CreateJobCommand command = new CreateJobCommand(
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                request.getBenefits(),
                request.getLocation(),
                request.getCity(),
                request.getAddress(),
                request.getJobType(),
                request.getExperienceLevel(),
                request.getMinSalary(),
                request.getMaxSalary(),
                request.getIsNegotiable(),
                request.getDeadline(),
                request.getSkills()
        );

        JobDetailResult result = jobUseCase.createJob(principal.getId(), command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng tin tuyển dụng thành công", JobResponse.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "HR cập nhật tin tuyển dụng", description = "Chỉnh sửa thông tin tin tuyển dụng")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {

        UpdateJobCommand command = new UpdateJobCommand(
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                request.getBenefits(),
                request.getLocation(),
                request.getCity(),
                request.getAddress(),
                request.getJobType(),
                request.getExperienceLevel(),
                request.getMinSalary(),
                request.getMaxSalary(),
                request.getIsNegotiable(),
                request.getDeadline(),
                request.getSkills()
        );

        JobDetailResult result = jobUseCase.updateJob(principal.getId(), id, command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tin tuyển dụng thành công", JobResponse.from(result)));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "HR đóng tin tuyển dụng", description = "Chuyển trạng thái tin tuyển dụng sang CLOSED")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        JobDetailResult result = jobUseCase.closeJob(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đóng tin tuyển dụng thành công", JobResponse.from(result)));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "HR xem danh sách tin tuyển dụng của mình", description = "Lấy danh sách tin tuyển dụng do HR đăng")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> getMyJobs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status) {

        int pageIndex = Math.max(0, page - 1);
        List<JobDetailResult> jobs = jobUseCase.getMyJobs(principal.getId(), pageIndex, size, status);
        long totalElements = jobUseCase.countMyJobs(principal.getId(), status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<JobResponse> content = jobs.stream().map(JobResponse::from).toList();
        PageResponse<JobResponse> pageResponse = PageResponse.<JobResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm việc làm công khai", description = "Tìm kiếm việc làm với bộ lọc đa tiêu chí (từ khóa, địa điểm, cấp bậc, mức lương...)")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageIndex = Math.max(0, page - 1);
        List<JobDetailResult> jobs = jobUseCase.searchJobs(keyword, location, jobType, experienceLevel, minSalary, maxSalary, pageIndex, size);
        long totalElements = jobUseCase.countSearchJobs(keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<JobResponse> content = jobs.stream().map(JobResponse::from).toList();
        PageResponse<JobResponse> pageResponse = PageResponse.<JobResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tin tuyển dụng công khai", description = "Xem chi tiết một tin tuyển dụng theo ID")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        JobDetailResult job = jobUseCase.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(JobResponse.from(job)));
    }
}
