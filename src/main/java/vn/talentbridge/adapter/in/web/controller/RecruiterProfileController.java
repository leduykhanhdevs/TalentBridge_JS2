package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.UpdateRecruiterProfileRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UpdateRecruiterProfileCommand;
import vn.talentbridge.core.application.port.in.GetRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.in.UpdateRecruiterProfileUseCase;

@RestController
@RequestMapping("/api/v1/recruiters")
@RequiredArgsConstructor
@Tag(name = "Recruiter Profile", description = "API quản lý hồ sơ Nhà tuyển dụng")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterProfileController {

    private final GetRecruiterProfileUseCase getRecruiterProfileUseCase;
    private final UpdateRecruiterProfileUseCase updateRecruiterProfileUseCase;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Lấy hồ sơ Nhà tuyển dụng hiện tại", description = "Lấy thông tin cá nhân và chức vụ của Nhà tuyển dụng đang đăng nhập")
    public ResponseEntity<ApiResponse<RecruiterResult>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        RecruiterResult result = getRecruiterProfileUseCase.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ nhà tuyển dụng thành công", result));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cập nhật hồ sơ Nhà tuyển dụng hiện tại", description = "Cập nhật họ tên, số điện thoại, avatarUrl và chức danh")
    public ResponseEntity<ApiResponse<RecruiterResult>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateRecruiterProfileRequest request) {

        UpdateRecruiterProfileCommand command = new UpdateRecruiterProfileCommand(
                request.getFullName(),
                request.getPhone(),
                request.getAvatarUrl(),
                request.getPosition());

        RecruiterResult result = updateRecruiterProfileUseCase.updateProfile(
                principal.getId(),
                command);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ nhà tuyển dụng thành công", result));
    }
}