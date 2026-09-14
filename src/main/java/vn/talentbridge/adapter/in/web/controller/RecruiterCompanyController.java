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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.RequestCreateCompanyRequest;
import vn.talentbridge.adapter.in.web.dto.response.CompanyResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.RequestCreateCompanyCommand;
import vn.talentbridge.core.application.port.in.RequestCreateCompanyUseCase;

@RestController
@RequestMapping("/api/v1/recruiters/companies")
@RequiredArgsConstructor
@Tag(name = "Recruiter Company", description = "API quản lý và yêu cầu mở công ty của Nhà tuyển dụng")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterCompanyController {

    private final RequestCreateCompanyUseCase requestCreateCompanyUseCase;

    @PostMapping("/request-create")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR yêu cầu tạo mới Doanh nghiệp", description = "Tạo doanh nghiệp ở trạng thái PENDING để Quản trị viên (Admin) xét duyệt")
    public ResponseEntity<ApiResponse<CompanyResponse>> requestCreateCompany(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RequestCreateCompanyRequest request) {

        RequestCreateCompanyCommand command = new RequestCreateCompanyCommand(
                request.getName(),
                request.getTaxCode(),
                request.getWebsite(),
                request.getCompanySize(),
                request.getAddress(),
                request.getCity(),
                request.getDescription(),
                request.getLogoUrl()
        );

        CompanyResult result = requestCreateCompanyUseCase.requestCreateCompany(principal.getId(), command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Yêu cầu tạo doanh nghiệp đã được gửi và đang chờ xét duyệt", CompanyResponse.from(result)));
    }
}
