package vn.talentbridge.adapter.in.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.AiMatchRequest;
import vn.talentbridge.adapter.in.web.dto.response.AiMatchResultResponse;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.common.ApiResponse;

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

    private final TriVectorRagMatchingService matchingService;

    @PostMapping("/match")
    public ResponseEntity<ApiResponse<AiMatchResultResponse>> matchCandidate(
            @Valid @RequestBody AiMatchRequest request) {
        log.info("[AI Matching] Yêu cầu so khớp Job ID: {}, Candidate ID: {}", request.getJobId(), request.getCandidateId());
        AiMatchResultResponse result = matchingService.matchCandidateToJob(
                request.getJobId(),
                request.getCandidateId(),
                request.getRawCvText()
        );
        return ResponseEntity.ok(ApiResponse.success("Đánh giá khớp nối ứng viên thành công", result));
    }
}
