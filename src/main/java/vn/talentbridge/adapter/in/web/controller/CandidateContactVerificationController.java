package vn.talentbridge.adapter.in.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.adapter.in.web.dto.request.VerifyContactOtpRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.ContactVerificationStatusResult;
import vn.talentbridge.core.application.port.in.ContactVerificationUseCase;

@RestController
@RequestMapping("/api/v1/candidates/contact-verification")
@RequiredArgsConstructor
public class CandidateContactVerificationController {

    private final ContactVerificationUseCase contactVerificationUseCase;

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ContactVerificationStatusResult>> getStatus(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(contactVerificationUseCase.getStatus(principal.getId())));
    }

    @PostMapping("/{channel}/request")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Void>> requestCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable ContactVerificationUseCase.Channel channel
    ) {
        contactVerificationUseCase.requestCode(principal.getId(), channel);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi mã xác minh.", null));
    }

    @PostMapping("/{channel}/verify")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ContactVerificationStatusResult>> verifyCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable ContactVerificationUseCase.Channel channel,
            @Valid @RequestBody VerifyContactOtpRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Xác minh thành công.",
                contactVerificationUseCase.verifyCode(principal.getId(), channel, request.getOtp())
        ));
    }
}
