package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.response.JobApplicantResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.port.in.GetCompanyJobsUseCase;
import vn.talentbridge.core.application.port.in.GetJobApplicantsUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Recruiter Jobs", description = "API quản lý tin tuyển dụng và xem danh sách ứng viên (HRPM-49)")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterJobController {

    private final GetJobApplicantsUseCase getJobApplicantsUseCase;
    private final GetCompanyJobsUseCase getCompanyJobsUseCase;

    @GetMapping({"/jobs/{jobId}/applicants", "/recruiters/jobs/{jobId}/applicants"})
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR xem danh sách ứng viên của một Job (HRPM-49)",
            description = "Trả về danh sách ứng viên đã nộp đơn vào Job thuộc công ty của HR")
    public ResponseEntity<ApiResponse<List<JobApplicantResponse>>> getJobApplicants(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId) {

        List<JobApplicantResult> results = getJobApplicantsUseCase.getJobApplicants(principal.getId(), jobId);
        List<JobApplicantResponse> response = results.stream()
                .map(JobApplicantResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/jobs/my-company", "/recruiters/jobs"})
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR lấy danh sách tin tuyển dụng của công ty mình",
            description = "Trả về danh sách tin tuyển dụng thuộc công ty của HR hiện tại")
    public ResponseEntity<ApiResponse<List<JobResult>>> getMyCompanyJobs(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<JobResult> results = getCompanyJobsUseCase.getCompanyJobs(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
