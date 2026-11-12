package vn.talentbridge.adapter.in.web.controller;

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
import vn.talentbridge.core.application.port.in.AssessMyJobFitUseCase;

@RestController
@RequestMapping("/api/v1/ai/jobs")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class CandidateJobFitController {
    private final AssessMyJobFitUseCase assessMyJobFitUseCase;

    @GetMapping("/{jobId}/my-fit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CandidateJobAssessmentResult>> assessMyFit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đánh giá mức độ phù hợp của hồ sơ với tin tuyển dụng",
                assessMyJobFitUseCase.assess(principal.getId(), jobId)));
    }
}
