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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.ApplyJobRequest;
import vn.talentbridge.adapter.in.web.dto.response.CandidateApplicationResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.ApplicationResult;
import vn.talentbridge.core.application.port.in.ApplyJobUseCase;
import vn.talentbridge.core.application.port.in.GetCandidateApplicationsUseCase;
import vn.talentbridge.core.application.port.in.WithdrawApplicationUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates/applications")
@RequiredArgsConstructor
@Tag(name = "Candidate Applications", description = "API ứng tuyển của ứng viên")
@SecurityRequirement(name = "BearerAuth")
public class ApplicationController {

    private final ApplyJobUseCase applyJobUseCase;
    private final WithdrawApplicationUseCase withdrawApplicationUseCase;
    private final GetCandidateApplicationsUseCase getCandidateApplicationsUseCase;

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Xem danh sách các đơn đã nộp của ứng viên (HRPM-42)")
    public ResponseEntity<ApiResponse<List<CandidateApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CandidateApplicationResponse> applications = getCandidateApplicationsUseCase
                .getMyApplications(principal.getId())
                .stream()
                .map(CandidateApplicationResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Nộp đơn ứng tuyển")
    public ResponseEntity<ApiResponse<ApplicationResult>> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplyJobRequest request) {

        ApplicationResult result = applyJobUseCase.apply(
                principal.getId(),
                request.getJobId(),
                request.getResumeId(),
                request.getCoverLetter()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Ứng tuyển thành công", result));
    }

    @PatchMapping("/{applicationId}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Rút đơn ứng tuyển của chính mình")
    public ResponseEntity<ApiResponse<ApplicationResult>> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId) {
        ApplicationResult result = withdrawApplicationUseCase.withdraw(principal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Rút đơn ứng tuyển thành công", result));
    }
}
