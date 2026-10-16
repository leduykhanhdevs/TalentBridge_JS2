package vn.talentbridge.adapter.in.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.AiMatchRequest;
import vn.talentbridge.adapter.in.web.dto.response.AiMatchResultResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;

/**
 * REST API for transparent, advisory CV-to-job similarity.
 * The project-defined three-vector lexical score is not an embedding model,
 * hiring probability, or formula attributed to the SoftwareX AI-KM paper.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiMatchingController {

    private final MatchApplicantUseCase matchApplicantUseCase;

    @PostMapping("/match")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<AiMatchResultResponse>> matchCandidate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiMatchRequest request) {
        log.info("[AI Matching] Recruiter {} yêu cầu so khớp Job ID: {}, Candidate ID: {}",
                principal.getId(), request.getJobId(), request.getCandidateId());
        AiMatchResultResponse result = AiMatchResultResponse.from(matchApplicantUseCase.matchApplicant(
                principal.getId(),
                request.getJobId(),
                request.getCandidateId()
        ));
        return ResponseEntity.ok(ApiResponse.success("Đánh giá khớp nối ứng viên thành công", result));
    }
}
