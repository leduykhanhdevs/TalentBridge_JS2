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
 * Controller cung cấp REST API đánh giá độ tương đồng giữa ứng viên và công việc
 * dựa trên phương pháp luận Tri-Vector RAG từ công trình SoftwareX 2025:
 * "Version 5.4.18 - AI-KM: Knowledge enhancement with RAG and workflow".
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
