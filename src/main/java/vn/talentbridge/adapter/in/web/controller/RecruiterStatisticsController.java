package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;
import vn.talentbridge.core.application.dto.JobQualityReportResult;
import vn.talentbridge.core.application.dto.RecruiterPipelineAnalyticsResult;
import vn.talentbridge.core.application.port.in.AnalyzeJobQualityUseCase;
import vn.talentbridge.core.application.port.in.AssessCandidateForJobUseCase;
import vn.talentbridge.core.application.port.in.GetRecruiterPipelineAnalyticsUseCase;

@RestController
@RequestMapping("/api/v1/statistics/recruiter/jobs/{jobId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterStatisticsController {
    private final AnalyzeJobQualityUseCase analyzeJobQualityUseCase;
    private final AssessCandidateForJobUseCase assessCandidateForJobUseCase;
    private final GetRecruiterPipelineAnalyticsUseCase pipelineAnalyticsUseCase;

    @GetMapping("/quality")
    @Operation(summary = "Đánh giá độ đầy đủ, rõ ràng và tín hiệu kiểm duyệt của tin tuyển dụng")
    public ResponseEntity<ApiResponse<JobQualityReportResult>> analyzeJobQuality(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                analyzeJobQualityUseCase.analyze(principal.getId(), jobId)));
    }

    @GetMapping("/candidates/{candidateId}")
    @Operation(summary = "Đánh giá tư vấn mức độ phù hợp hồ sơ ứng viên với tin tuyển dụng")
    public ResponseEntity<ApiResponse<CandidateJobAssessmentResult>> assessCandidate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessCandidateForJobUseCase.assess(principal.getId(), jobId, candidateId)));
    }

    @GetMapping("/pipeline")
    @Operation(summary = "Phân bố đơn ứng tuyển theo giai đoạn ATS hiện tại")
    public ResponseEntity<ApiResponse<RecruiterPipelineAnalyticsResult>> pipeline(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                pipelineAnalyticsUseCase.getForJob(principal.getId(), jobId)));
    }
}
