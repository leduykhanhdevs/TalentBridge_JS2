package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.response.JobResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruiters/my-jobs")
@RequiredArgsConstructor
@Tag(name = "Recruiter Job Management", description = "API dành riêng cho Nhà tuyển dụng xem tin đăng của mình")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterJobController {

    private final JobUseCase jobUseCase;

    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR xem danh sách tin tuyển dụng của mình", description = "Lấy danh sách tin tuyển dụng do HR hiện tại đăng tuyển")
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
}
