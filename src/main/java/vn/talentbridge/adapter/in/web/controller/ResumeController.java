package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.ResumeResult;
import vn.talentbridge.core.application.port.in.ResumeUseCase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates/resumes")
@RequiredArgsConstructor
@Tag(name = "Candidate Resumes", description = "API quản lý tải lên và quản lý hồ sơ CV của ứng viên (PDF/DOCX tối đa 10MB)")
public class ResumeController {

    private final ResumeUseCase resumeUseCase;

    @GetMapping
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Lấy danh sách CV của ứng viên hiện tại")
    public ResponseEntity<ApiResponse<List<ResumeResult>>> getResumes(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ResumeResult> resumes = resumeUseCase.getResumes(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách CV thành công", resumes));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Tải lên CV (PDF hoặc DOCX, tối đa 10MB)")
    public ResponseEntity<ApiResponse<ResumeResult>> uploadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file CV để tải lên.");
        }

        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String contentType = file.getContentType();

        ResumeResult result = resumeUseCase.uploadResume(principal.getId(), title, originalFilename, contentType, bytes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải lên CV thành công", result));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Xóa CV của ứng viên")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") Long id) {
        resumeUseCase.deleteResume(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa CV thành công", null));
    }

    @PutMapping("/{id}/default")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Đặt CV làm CV mặc định")
    public ResponseEntity<ApiResponse<ResumeResult>> setDefaultResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") Long id) {
        ResumeResult result = resumeUseCase.setDefaultResume(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã đặt làm CV mặc định", result));
    }

    @GetMapping("/{id}/download")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER', 'ADMIN')")
    @Operation(summary = "Tải xuống file CV")
    public ResponseEntity<byte[]> downloadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") Long id) {
        ResumeResult resume = resumeUseCase.getResume(principal.getId(), id);
        byte[] content = resumeUseCase.downloadResume(principal.getId(), id);

        String encodedFilename = URLEncoder.encode(resume.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + resume.fileName() + "\"; filename*=UTF-8''" + encodedFilename;

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (resume.fileType() != null && !resume.fileType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(resume.fileType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(content);
    }
}
