package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.ForgotPasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.ResetPasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.VerifyPasswordResetOtpRequest;
import vn.talentbridge.adapter.in.web.dto.response.PasswordResetVerificationResponse;
import vn.talentbridge.adapter.in.web.exception.ErrorCode;
import vn.talentbridge.adapter.in.web.passwordreset.PasswordResetRequestDispatcher;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.port.in.ResetPasswordUseCase;
import vn.talentbridge.core.application.port.in.VerifyPasswordResetOtpUseCase;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Password Reset",
        description = "Quên và đặt lại mật khẩu (HRPM-11)"
)
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(
            PasswordResetController.class
    );

    private final PasswordResetRequestDispatcher passwordResetRequestDispatcher;
    private final VerifyPasswordResetOtpUseCase verifyPasswordResetOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/forgot-password")
    @Operation(summary = "Gửi mã OTP 6 chữ số đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        try {
            passwordResetRequestDispatcher.dispatch(request.getEmail());
        } catch (TaskRejectedException e) {
            // Queue saturation happens before account lookup, so a generic retryable
            // response does not reveal whether the submitted email exists.
            log.warn("Password reset request was not queued because the background queue is full");
            return ResponseEntity.status(ErrorCode.TEMPORARY_SERVICE_UNAVAILABLE.getHttpStatus())
                    .body(ApiResponse.error(
                            ErrorCode.TEMPORARY_SERVICE_UNAVAILABLE.getCode(),
                            ErrorCode.TEMPORARY_SERVICE_UNAVAILABLE.getMessage()
                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Nếu email tồn tại trong hệ thống, "
                                + "mã OTP sẽ được gửi",
                        null
                )
        );
    }

    @PostMapping("/verify-reset-otp")
    @Transactional
    @Operation(summary = "Xác minh OTP trước khi cho phép đặt mật khẩu mới")
    public ResponseEntity<ApiResponse<PasswordResetVerificationResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyPasswordResetOtpRequest request
    ) {
        return verifyPasswordResetOtpUseCase.verifyOtp(
                        request.getEmail(),
                        request.getOtp()
                )
                .map(resetToken -> ResponseEntity.ok(
                        ApiResponse.success(
                                "Mã OTP hợp lệ. Bạn có thể đặt mật khẩu mới.",
                                new PasswordResetVerificationResponse(resetToken)
                        )
                ))
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        ApiResponse.error(
                                40005,
                                "Mã OTP không chính xác hoặc đã hết hạn. "
                                        + "Bạn có thể nhập lại hoặc yêu cầu mã mới."
                        )
                ));
    }

    @PostMapping("/reset-password")
    @Transactional
    @Operation(summary = "Đặt mật khẩu mới bằng mã xác minh OTP đã cấp")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        resetPasswordUseCase.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Đặt lại mật khẩu thành công",
                        null
                )
        );
    }
}
