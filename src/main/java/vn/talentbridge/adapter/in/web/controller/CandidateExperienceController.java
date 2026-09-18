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
import vn.talentbridge.adapter.in.web.dto.request.CandidateSkillRequest;
import vn.talentbridge.adapter.in.web.dto.request.WorkExperienceRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.*;
import vn.talentbridge.core.application.port.in.CandidateSkillUseCase;
import vn.talentbridge.core.application.port.in.WorkExperienceUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Candidate Experiences & Skills", description = "API quản lý Quá trình làm việc và Kỹ năng chuyên môn của Ứng viên (TopCV standard)")
public class CandidateExperienceController {

    private final WorkExperienceUseCase workExperienceUseCase;
    private final CandidateSkillUseCase candidateSkillUseCase;

    // ==========================================
    // 1. WORK EXPERIENCES
    // ==========================================

    @GetMapping("/candidates/work-experiences")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Lấy danh sách kinh nghiệm làm việc của ứng viên hiện tại")
    public ResponseEntity<ApiResponse<List<WorkExperienceResult>>> getWorkExperiences(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<WorkExperienceResult> result = workExperienceUseCase.getWorkExperiences(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kinh nghiệm làm việc thành công", result));
    }

    @PostMapping("/candidates/work-experiences")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Thêm kinh nghiệm làm việc mới")
    public ResponseEntity<ApiResponse<WorkExperienceResult>> addWorkExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WorkExperienceRequest request) {

        WorkExperienceCommand command = new WorkExperienceCommand(
                request.getCompanyName(),
                request.getPosition(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsCurrent(),
                request.getDescription(),
                request.getAchievements()
        );

        WorkExperienceResult result = workExperienceUseCase.addWorkExperience(principal.getId(), command);
        return new ResponseEntity<>(ApiResponse.created("Thêm kinh nghiệm làm việc thành công", result), HttpStatus.CREATED);
    }

    @PutMapping("/candidates/work-experiences/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Cập nhật kinh nghiệm làm việc")
    public ResponseEntity<ApiResponse<WorkExperienceResult>> updateWorkExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody WorkExperienceRequest request) {

        WorkExperienceCommand command = new WorkExperienceCommand(
                request.getCompanyName(),
                request.getPosition(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsCurrent(),
                request.getDescription(),
                request.getAchievements()
        );

        WorkExperienceResult result = workExperienceUseCase.updateWorkExperience(principal.getId(), id, command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật kinh nghiệm làm việc thành công", result));
    }

    @DeleteMapping("/candidates/work-experiences/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Xóa kinh nghiệm làm việc")
    public ResponseEntity<ApiResponse<Void>> deleteWorkExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        workExperienceUseCase.deleteWorkExperience(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa kinh nghiệm làm việc thành công", null));
    }

    // ==========================================
    // 2. CANDIDATE SKILLS
    // ==========================================

    @GetMapping("/candidates/skills")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Lấy danh sách kỹ năng chuyên môn của ứng viên hiện tại")
    public ResponseEntity<ApiResponse<List<CandidateSkillResult>>> getCandidateSkills(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CandidateSkillResult> result = candidateSkillUseCase.getCandidateSkills(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỹ năng thành công", result));
    }

    @PostMapping("/candidates/skills")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Thêm hoặc cập nhật đánh giá kỹ năng chuyên môn (1-5 sao)")
    public ResponseEntity<ApiResponse<CandidateSkillResult>> addCandidateSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidateSkillRequest request) {

        AddCandidateSkillCommand command = new AddCandidateSkillCommand(
                request.getSkillId(),
                request.getSkillName(),
                request.getProficiencyLevel(),
                request.getRating(),
                request.getYearsOfExperience()
        );

        CandidateSkillResult result = candidateSkillUseCase.addCandidateSkill(principal.getId(), command);
        return new ResponseEntity<>(ApiResponse.created("Cập nhật kỹ năng thành công", result), HttpStatus.CREATED);
    }

    @DeleteMapping("/candidates/skills/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Xóa kỹ năng chuyên môn của ứng viên")
    public ResponseEntity<ApiResponse<Void>> deleteCandidateSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        candidateSkillUseCase.deleteCandidateSkill(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa kỹ năng thành công", null));
    }

    // ==========================================
    // 3. MASTER SKILLS CATALOG
    // ==========================================

    @GetMapping("/skills")
    @Operation(summary = "Lấy danh mục kỹ năng hệ thống (Autocomplete/Dropdown)")
    public ResponseEntity<ApiResponse<List<SkillResult>>> getAllSkills() {
        List<SkillResult> result = candidateSkillUseCase.getAllAvailableSkills();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục kỹ năng thành công", result));
    }
}
