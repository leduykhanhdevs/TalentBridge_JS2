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
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.RateAndNoteApplicantRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateApplicantStatusRequest;
import vn.talentbridge.adapter.in.web.dto.response.ApplicationNoteResponse;
import vn.talentbridge.adapter.in.web.dto.response.ApplicationStageResponse;
import vn.talentbridge.adapter.in.web.dto.response.JobApplicantResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.*;
import vn.talentbridge.core.application.port.in.GetJobApplicantsUseCase;
import vn.talentbridge.core.application.port.in.RateAndNoteApplicantUseCase;
import vn.talentbridge.core.application.port.in.UpdateApplicantStatusUseCase;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Recruiter Applicant Screening", description = "API sàng lọc hồ sơ, lọc, sắp xếp, đổi trạng thái và ghi chú ứng viên")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterApplicantController {

    private final GetJobApplicantsUseCase getJobApplicantsUseCase;
    private final UpdateApplicantStatusUseCase updateApplicantStatusUseCase;
    private final RateAndNoteApplicantUseCase rateAndNoteApplicantUseCase;

    @GetMapping({
            "/api/v1/recruiters/jobs/{jobId}/applicants",
            "/api/v1/jobs/{jobId}/applicants"
    })
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR xem danh sách ứng viên của tin tuyển dụng", description = "Hỗ trợ tìm kiếm từ khóa, lọc theo stage/kinh nghiệm, và sắp xếp linh hoạt")
    public ResponseEntity<ApiResponse<List<JobApplicantResponse>>> getApplicants(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ApplicantFilterCriteria criteria = new ApplicantFilterCriteria(
                keyword, stage, minExperience, sortBy, sortDirection
        );

        List<JobApplicantResult> results = getJobApplicantsUseCase.getJobApplicants(principal.getId(), jobId, criteria);
        List<JobApplicantResponse> response = results.stream().map(JobApplicantResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping({
            "/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/stage",
            "/api/v1/jobs/{jobId}/applicants/{applicationId}/stage",
            "/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/status"
    })
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cập nhật vòng tuyển dụng/trạng thái của ứng viên", description = "Chuyển stage (APPLIED, REVIEWING, SHORTLISTED, INTERVIEW, OFFERED, HIRED, REJECTED) và lưu audit history")
    public ResponseEntity<ApiResponse<JobApplicantResponse>> updateStage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicantStatusRequest request) {

        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand(
                request.getStage(),
                request.getStatus(),
                request.getNote()
        );

        JobApplicantResult result = updateApplicantStatusUseCase.updateStageAndStatus(
                principal.getId(), jobId, applicationId, command
        );

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ứng viên thành công", JobApplicantResponse.from(result)));
    }

    @PostMapping("/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/notes")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Đánh giá sao và thêm ghi chú nội bộ cho ứng viên")
    public ResponseEntity<ApiResponse<ApplicationNoteResponse>> addNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @Valid @RequestBody RateAndNoteApplicantRequest request) {

        RateAndNoteApplicantCommand command = new RateAndNoteApplicantCommand(
                request.getRating(),
                request.getTag(),
                request.getComment()
        );

        ApplicationNoteResult result = rateAndNoteApplicantUseCase.addNote(
                principal.getId(), jobId, applicationId, command
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm ghi chú đánh giá thành công", ApplicationNoteResponse.from(result)));
    }

    @GetMapping("/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/notes")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Xem lịch sử ghi chú nội bộ và đánh giá ứng viên")
    public ResponseEntity<ApiResponse<List<ApplicationNoteResponse>>> getNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long applicationId) {

        List<ApplicationNoteResult> results = rateAndNoteApplicantUseCase.getNotes(
                principal.getId(), jobId, applicationId
        );
        List<ApplicationNoteResponse> response = results.stream().map(ApplicationNoteResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/stages")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Xem lịch sử chuyển vòng tuyển dụng của ứng viên")
    public ResponseEntity<ApiResponse<List<ApplicationStageResponse>>> getStageHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long applicationId) {

        List<ApplicationStageResult> results = rateAndNoteApplicantUseCase.getStageHistory(
                principal.getId(), jobId, applicationId
        );
        List<ApplicationStageResponse> response = results.stream().map(ApplicationStageResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
