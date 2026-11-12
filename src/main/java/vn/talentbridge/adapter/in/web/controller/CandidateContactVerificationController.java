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
import vn.talentbridge.core.application.dto.ContactVerificationRequestResult;
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
    public ResponseEntity<ApiResponse<ContactVerificationRequestResult>> requestCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable ContactVerificationUseCase.Channel channel
    ) {
        ContactVerificationRequestResult result = contactVerificationUseCase.requestCode(principal.getId(), channel);
        String message;
        if (result.alreadyVerified()) {
            message = "Thông tin liên hệ đã được xác minh trước đó.";
        } else if (!result.deliveryAccepted()) {
            message = "Chưa có xác nhận gửi mã từ nhà cung cấp.";
        } else if (channel == ContactVerificationUseCase.Channel.EMAIL) {
            message = "Nhà cung cấp đã chấp nhận yêu cầu gửi mã tới " + result.destination()
                    + ". Điều này chưa xác nhận thư đã vào hộp thư; hãy kiểm tra Inbox và Spam.";
        } else {
            message = "Nhà cung cấp đã chấp nhận yêu cầu gửi mã tới " + result.destination()
                    + ". Điều này chưa xác nhận tin nhắn đã đến điện thoại.";
        }
        return ResponseEntity.ok(ApiResponse.success(message, result));
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
