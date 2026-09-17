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
import vn.talentbridge.adapter.in.web.dto.request.UpdateCandidateProfileRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.CandidateResult;
import vn.talentbridge.core.application.dto.UpdateCandidateProfileCommand;
import vn.talentbridge.core.application.port.in.GetCandidateProfileUseCase;
import vn.talentbridge.core.application.port.in.UpdateCandidateProfileUseCase;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate Profile", description = "API quản lý hồ sơ Ứng viên (Candidate)")
@SecurityRequirement(name = "BearerAuth")
public class CandidateProfileController {

    private final GetCandidateProfileUseCase getCandidateProfileUseCase;
    private final UpdateCandidateProfileUseCase updateCandidateProfileUseCase;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Lấy hồ sơ Ứng viên hiện tại", description = "Lấy thông tin cá nhân, nghề nghiệp và liên kết xã hội của ứng viên đang đăng nhập")
    public ResponseEntity<ApiResponse<CandidateResult>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        CandidateResult result = getCandidateProfileUseCase.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ ứng viên thành công", result));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Cập nhật hồ sơ Ứng viên", description = "Cập nhật họ tên, SĐT, chức danh, mức lương, kinh nghiệm và liên kết mạng xã hội")
    public ResponseEntity<ApiResponse<CandidateResult>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCandidateProfileRequest request) {

        UpdateCandidateProfileCommand command = new UpdateCandidateProfileCommand(
                request.getFullName(),
                request.getPhone(),
                request.getAvatarUrl(),
                request.getTitle(),
                request.getDob(),
                request.getGender(),
                request.getSummary(),
                request.getExperienceYears(),
                request.getCurrentSalary(),
                request.getExpectedSalary(),
                request.getCity(),
                request.getAddress(),
                request.getPersonalWebsite(),
                request.getLinkedinUrl(),
                request.getGithubUrl()
        );

        CandidateResult result = updateCandidateProfileUseCase.updateProfile(principal.getId(), command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ ứng viên thành công", result));
    }
}
