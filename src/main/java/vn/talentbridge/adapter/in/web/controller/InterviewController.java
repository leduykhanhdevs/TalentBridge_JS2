package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.ScheduleInterviewRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.InterviewResult;
import vn.talentbridge.core.application.dto.ScheduleInterviewCommand;
import vn.talentbridge.core.application.port.in.ScheduleInterviewUseCase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Interview Scheduling", description = "API đặt lịch phỏng vấn và đồng bộ Google Calendar")
public class InterviewController {

    private final ScheduleInterviewUseCase scheduleInterviewUseCase;

    @PostMapping({
            "/api/v1/recruiters/applications/{applicationId}/interviews",
            "/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/interviews"
    })
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR lên lịch phỏng vấn cho ứng viên", description = "Lưu lịch phỏng vấn, tự động chuyển stage sang INTERVIEW và sinh link Google Calendar")
    public ResponseEntity<ApiResponse<InterviewResult>> scheduleInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId,
            @PathVariable(required = false) Long jobId,
            @Valid @RequestBody ScheduleInterviewRequest request) {

        ScheduleInterviewCommand command = new ScheduleInterviewCommand(
                request.getInterviewTime(),
                request.getLocationType(),
                request.getMeetingLinkOrAddress(),
                request.getNotes()
        );

        InterviewResult result = scheduleInterviewUseCase.scheduleInterview(principal.getId(), applicationId, command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Lên lịch phỏng vấn thành công", result));
    }

    @GetMapping({
            "/api/v1/recruiters/applications/{applicationId}/interviews",
            "/api/v1/recruiters/jobs/{jobId}/applicants/{applicationId}/interviews"
    })
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "HR xem danh sách lịch phỏng vấn của hồ sơ ứng viên")
    public ResponseEntity<ApiResponse<List<InterviewResult>>> getRecruiterInterviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId,
            @PathVariable(required = false) Long jobId) {

        List<InterviewResult> results = scheduleInterviewUseCase.getInterviewsByApplication(principal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/api/v1/candidates/applications/{applicationId}/interview")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên xem lịch phỏng vấn của đơn ứng tuyển")
    public ResponseEntity<ApiResponse<InterviewResult>> getCandidateInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId) {

        InterviewResult result = scheduleInterviewUseCase.getLatestInterviewForApplication(principal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/v1/interviews/{interviewId}/calendar.ics")
    @Operation(summary = "Tải file lịch iCalendar (.ics) đồng bộ Google Calendar / Outlook")
    public ResponseEntity<byte[]> downloadIcsCalendar(@PathVariable Long interviewId) {
        byte[] icsBytes = scheduleInterviewUseCase.exportIcsCalendar(interviewId);
        String filename = "interview-" + interviewId + ".ics";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(icsBytes);
    }
}
