package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.ForgotPasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.ResetPasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.VerifyPasswordResetOtpRequest;
import vn.talentbridge.adapter.in.web.dto.response.PasswordResetVerificationResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;
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

    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final VerifyPasswordResetOtpUseCase verifyPasswordResetOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/forgot-password")
    @Transactional
    @Operation(summary = "Gửi mã OTP 6 chữ số đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        forgotPasswordUseCase.requestPasswordReset(
                request.getEmail()
        );

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
