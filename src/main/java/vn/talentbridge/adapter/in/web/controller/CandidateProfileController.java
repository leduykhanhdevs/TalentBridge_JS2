package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.ApplyParsedCvRequest;
import vn.talentbridge.adapter.in.web.dto.request.UpdateCandidateProfileRequest;
import vn.talentbridge.adapter.in.web.dto.response.AvatarUploadResult;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.*;
import vn.talentbridge.core.application.port.in.CandidateAvatarUseCase;
import vn.talentbridge.core.application.port.in.CandidateSkillUseCase;
import vn.talentbridge.core.application.port.in.GetCandidateProfileUseCase;
import vn.talentbridge.core.application.port.in.ParseCvUseCase;
import vn.talentbridge.core.application.port.in.UpdateCandidateProfileUseCase;
import vn.talentbridge.core.application.port.in.WorkExperienceUseCase;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate Profile", description = "API quản lý hồ sơ Ứng viên (Candidate)")
@SecurityRequirement(name = "BearerAuth")
public class CandidateProfileController {

    private final GetCandidateProfileUseCase getCandidateProfileUseCase;
    private final UpdateCandidateProfileUseCase updateCandidateProfileUseCase;
    private final CandidateSkillUseCase candidateSkillUseCase;
    private final WorkExperienceUseCase workExperienceUseCase;
    private final ParseCvUseCase parseCvUseCase;
    private final CandidateAvatarUseCase candidateAvatarUseCase;

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

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Tải ảnh đại diện ứng viên lên", description = "Hỗ trợ PNG/JPEG tối đa 5 MB")
    public ResponseEntity<ApiResponse<AvatarUploadResult>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện.");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5 MB.");
        }

        try {
            String fileName = candidateAvatarUseCase.upload(file.getBytes());
            String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/candidates/profile/avatar/{fileName}")
                    .buildAndExpand(fileName)
                    .toUriString();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Tải ảnh đại diện thành công", new AvatarUploadResult(avatarUrl)));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ảnh không hợp lệ hoặc không thể lưu được.", e);
        }
    }

    @GetMapping("/profile/avatar/{fileName}")
    @Operation(summary = "Lấy ảnh đại diện ứng viên")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String fileName) {
        if (!fileName.matches("^[a-fA-F0-9-]{36}\\.(?:jpg|png)$")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            byte[] bytes = candidateAvatarUseCase.load(fileName);
            MediaType contentType = fileName.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                    .body(bytes);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/profile/parse-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Trích xuất thông tin tự động từ file CV", description = "Tải lên file PDF hoặc DOCX để hệ thống tự động bóc tách họ tên, SĐT, email, kỹ năng và kinh nghiệm")
    public ResponseEntity<ApiResponse<ParsedCvResult>> parseCv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file CV để bóc tách thông tin.");
        }

        try {
            byte[] bytes = file.getBytes();
            ParsedCvResult result = parseCvUseCase.parseCv(bytes, file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.success("Trích xuất thông tin CV thành công", result));
        } catch (Exception e) {
            log.error("Lỗi trích xuất CV: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đọc nội dung file CV: " + e.getMessage());
        }
    }

    @PostMapping("/profile/apply-parsed-cv")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Cập nhật hồ sơ từ kết quả bóc tách CV", description = "Ghi đè hoặc bổ sung các trường thông tin cá nhân, kỹ năng và kinh nghiệm từ CV vào hồ sơ TalentBridge")
    public ResponseEntity<ApiResponse<CandidateResult>> applyParsedCv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ApplyParsedCvRequest request) {

        CandidateResult current = getCandidateProfileUseCase.getProfile(principal.getId());

        String fullName = (request.getFullName() != null && !request.getFullName().isBlank()) ? request.getFullName().trim() : current.fullName();
        String phone = (request.getPhone() != null && !request.getPhone().isBlank()) ? request.getPhone().trim() : current.phone();
        String title = (request.getTitle() != null && !request.getTitle().isBlank()) ? request.getTitle().trim() : current.title();
        String city = (request.getCity() != null && !request.getCity().isBlank()) ? request.getCity().trim() : current.city();
        String summary = (request.getSummary() != null && !request.getSummary().isBlank()) ? request.getSummary().trim() : current.summary();

        UpdateCandidateProfileCommand command = new UpdateCandidateProfileCommand(
                fullName,
                phone,
                current.avatarUrl(),
                title,
                current.dob(),
                current.gender(),
                summary,
                current.experienceYears(),
                current.currentSalary(),
                current.expectedSalary(),
                city,
                current.address(),
                current.personalWebsite(),
                current.linkedinUrl(),
                current.githubUrl()
        );

        CandidateResult updated = updateCandidateProfileUseCase.updateProfile(principal.getId(), command);

        // Apply skills if provided
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<CandidateSkillResult> existingSkills = candidateSkillUseCase.getCandidateSkills(principal.getId());
            for (String skillName : request.getSkills()) {
                if (skillName == null || skillName.isBlank()) continue;
                boolean alreadyHas = existingSkills.stream().anyMatch(s -> s.skillName().equalsIgnoreCase(skillName.trim()));
                if (!alreadyHas) {
                    try {
                        candidateSkillUseCase.addCandidateSkill(
                                principal.getId(),
                                new AddCandidateSkillCommand(null, skillName.trim(), "INTERMEDIATE", 4, 1.0)
                        );
                    } catch (Exception ex) {
                        log.warn("Không thể lưu kỹ năng '{}': {}", skillName, ex.getMessage());
                    }
                }
            }
        }

        // Apply work experiences if provided
        if (request.getExperiences() != null && !request.getExperiences().isEmpty()) {
            for (ApplyParsedCvRequest.ParsedExperienceRequest exp : request.getExperiences()) {
                if (exp.getCompanyName() == null || exp.getCompanyName().isBlank()) continue;

                LocalDate startDate = parseDateSafe(exp.getStartDate(), LocalDate.now().minusYears(1));
                LocalDate endDate = Boolean.TRUE.equals(exp.getIsCurrent()) ? null : parseDateSafe(exp.getEndDate(), null);

                try {
                    workExperienceUseCase.addWorkExperience(
                            principal.getId(),
                            new WorkExperienceCommand(
                                    exp.getCompanyName().trim(),
                                    exp.getPosition() != null && !exp.getPosition().isBlank() ? exp.getPosition().trim() : "Chuyên viên",
                                    startDate,
                                    endDate,
                                    Boolean.TRUE.equals(exp.getIsCurrent()),
                                    exp.getDescription(),
                                    null
                            )
                    );
                } catch (Exception ex) {
                    log.warn("Không thể lưu kinh nghiệm làm việc '{}': {}", exp.getCompanyName(), ex.getMessage());
                }
            }
        }

        CandidateResult finalResult = getCandidateProfileUseCase.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đồng bộ thông tin CV vào hồ sơ thành công", finalResult));
    }

    private LocalDate parseDateSafe(String text, LocalDate fallback) {
        if (text == null || text.isBlank()) return fallback;
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            try {
                if (text.matches("^\\d{4}$")) {
                    return LocalDate.of(Integer.parseInt(text), 1, 1);
                }
            } catch (Exception ignored) {}
            return fallback;
        }
    }
}
