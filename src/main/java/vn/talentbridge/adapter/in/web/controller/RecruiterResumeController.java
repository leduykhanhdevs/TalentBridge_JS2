package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.core.application.dto.ApplicantResumeDownload;
import vn.talentbridge.core.application.port.in.DownloadApplicantResumeUseCase;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/recruiters/jobs/{jobId}/resumes")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class RecruiterResumeController {
    private final DownloadApplicantResumeUseCase downloadApplicantResumeUseCase;

    @GetMapping("/{resumeId}/view")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<byte[]> viewApplicantResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long resumeId) {
        ApplicantResumeDownload resume = downloadApplicantResumeUseCase.downloadForRecruiter(
                principal.getId(), jobId, resumeId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (resume.fileType() != null && !resume.fileType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(resume.fileType());
            } catch (IllegalArgumentException ignored) {
                // Use the safe binary fallback when stored metadata is malformed.
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(resume.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resume.content());
    }
}
