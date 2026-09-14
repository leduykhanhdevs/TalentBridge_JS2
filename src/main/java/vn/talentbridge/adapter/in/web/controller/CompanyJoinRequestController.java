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
import vn.talentbridge.adapter.in.web.dto.request.SubmitJoinCompanyRequest;
import vn.talentbridge.adapter.in.web.dto.response.CompanyJoinRequestResponse;
import vn.talentbridge.adapter.in.web.dto.response.CompanyResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.common.PageResponse;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.SubmitJoinCompanyRequestCommand;
import vn.talentbridge.core.application.port.in.SearchApprovedCompaniesUseCase;
import vn.talentbridge.core.application.port.in.SubmitJoinCompanyRequestUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruiters/companies")
@RequiredArgsConstructor
@Tag(name = "Recruiter Company Join Request", description = "API tìm kiếm công ty và gửi yêu cầu gia nhập công ty dành cho HR")
@SecurityRequirement(name = "BearerAuth")
public class CompanyJoinRequestController {

    private final SearchApprovedCompaniesUseCase searchApprovedCompaniesUseCase;
    private final SubmitJoinCompanyRequestUseCase submitJoinCompanyRequestUseCase;

    @GetMapping("/search")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Tìm kiếm công ty đã được phê duyệt", description = "Tìm kiếm danh sách công ty đã có trạng thái APPROVED để HR chọn gửi yêu cầu gia nhập")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> searchApprovedCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        int pageIndex = Math.max(0, page - 1);
        List<CompanyResult> companies = searchApprovedCompaniesUseCase.searchApprovedCompanies(keyword, pageIndex, size);
        long totalElements = searchApprovedCompaniesUseCase.countApprovedCompanies(keyword);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<CompanyResponse> content = companies.stream().map(CompanyResponse::from).toList();
        PageResponse<CompanyResponse> pageResponse = PageResponse.<CompanyResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages || totalPages == 0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PostMapping("/{companyId}/join-request")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Gửi yêu cầu xin gia nhập công ty", description = "Gửi yêu cầu gia nhập vào một công ty đã được phê duyệt kèm vị trí ứng tuyển và lời nhắn")
    public ResponseEntity<ApiResponse<CompanyJoinRequestResponse>> submitJoinRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long companyId,
            @Valid @RequestBody SubmitJoinCompanyRequest request) {

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand(
                request.getPosition(),
                request.getMessage()
        );

        CompanyJoinRequestResult result = submitJoinCompanyRequestUseCase.submitJoinRequest(
                principal.getId(),
                companyId,
                command
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Gửi yêu cầu xin gia nhập công ty thành công", CompanyJoinRequestResponse.from(result)));
    }
}
