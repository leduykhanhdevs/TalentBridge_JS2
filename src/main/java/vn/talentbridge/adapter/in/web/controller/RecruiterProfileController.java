package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.response.AvatarUploadResult;
import vn.talentbridge.adapter.in.web.dto.request.UpdateRecruiterProfileRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UpdateRecruiterProfileCommand;
import vn.talentbridge.core.application.port.in.GetRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.in.UpdateRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.in.CandidateAvatarUseCase;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/recruiters")
@RequiredArgsConstructor
@Tag(name = "Recruiter Profile", description = "API quản lý hồ sơ Nhà tuyển dụng")
@SecurityRequirement(name = "BearerAuth")
public class RecruiterProfileController {

    private final GetRecruiterProfileUseCase getRecruiterProfileUseCase;
    private final UpdateRecruiterProfileUseCase updateRecruiterProfileUseCase;
    private final CandidateAvatarUseCase candidateAvatarUseCase;

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

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Tải ảnh đại diện nhà tuyển dụng lên", description = "Hỗ trợ PNG/JPEG tối đa 5 MB")
    public ResponseEntity<ApiResponse<AvatarUploadResult>> uploadAvatar(@RequestParam("file") MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện.");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5 MB.");
        }
        if (!MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(file.getContentType())
                && !MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Ảnh đại diện chỉ hỗ trợ JPG hoặc PNG.");
        }

        String fileName = candidateAvatarUseCase.upload(file.getBytes());
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/candidates/profile/avatar/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tải ảnh đại diện thành công", new AvatarUploadResult(avatarUrl)));
    }
}
