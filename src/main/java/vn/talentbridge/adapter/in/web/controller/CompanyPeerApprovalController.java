package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.ReviewJoinRequest;
import vn.talentbridge.adapter.in.web.dto.response.CompanyJoinRequestResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.ReviewJoinRequestCommand;
import vn.talentbridge.core.application.port.in.GetCompanyJoinRequestsUseCase;
import vn.talentbridge.core.application.port.in.ReviewJoinRequestUseCase;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruiters/companies")
@RequiredArgsConstructor
@Tag(name = "Recruiter Peer Approval", description = "API quản lý và phê duyệt yêu cầu gia nhập nội bộ công ty dành cho HR")
@SecurityRequirement(name = "BearerAuth")
public class CompanyPeerApprovalController {

    private final GetCompanyJoinRequestsUseCase getCompanyJoinRequestsUseCase;
    private final ReviewJoinRequestUseCase reviewJoinRequestUseCase;

    @GetMapping("/my-company/join-requests")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR xem danh sách yêu cầu gia nhập công ty mình", description = "Lấy danh sách các yêu cầu gia nhập đang chờ duyệt hoặc đã xử lý của công ty HR hiện tại đang làm việc")
    public ResponseEntity<ApiResponse<PageResponse<CompanyJoinRequestResponse>>> getCompanyJoinRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CompanyJoinRequestStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageIndex = Math.max(0, page - 1);
        List<CompanyJoinRequestResult> results = getCompanyJoinRequestsUseCase.getCompanyJoinRequests(
                principal.getId(),
                status,
                pageIndex,
                size
        );

        long totalElements = getCompanyJoinRequestsUseCase.countCompanyJoinRequests(principal.getId(), status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<CompanyJoinRequestResponse> content = results.stream()
                .map(CompanyJoinRequestResponse::from)
                .toList();

        PageResponse<CompanyJoinRequestResponse> pageResponse = PageResponse.<CompanyJoinRequestResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages || totalPages == 0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PatchMapping("/join-requests/{requestId}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Transactional
    @Operation(summary = "HR phê duyệt hoặc từ chối yêu cầu gia nhập", description = "Phê duyệt (ACCEPTED) hoặc từ chối (REJECTED) yêu cầu gia nhập của HR khác vào công ty mình")
    public ResponseEntity<ApiResponse<CompanyJoinRequestResponse>> reviewJoinRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @Valid @RequestBody ReviewJoinRequest request) {

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(
                request.getStatus(),
                request.getReason()
        );

        CompanyJoinRequestResult result = reviewJoinRequestUseCase.reviewJoinRequest(
                principal.getId(),
                requestId,
                command
        );

        return ResponseEntity.ok(ApiResponse.success("Xử lý yêu cầu gia nhập công ty thành công", CompanyJoinRequestResponse.from(result)));
    }
}
