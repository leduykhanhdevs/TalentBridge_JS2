package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.ForgotPasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.ResetPasswordRequest;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;
import vn.talentbridge.core.application.port.in.ResetPasswordUseCase;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Password Reset",
        description = "Quên và đặt lại mật khẩu (HRPM-11)"
)
public class PasswordResetController {

    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu liên kết đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        forgotPasswordUseCase.requestPasswordReset(
                request.getEmail()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Nếu email tồn tại trong hệ thống, "
                                + "liên kết đặt lại mật khẩu sẽ được gửi",
                        null
                )
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu bằng token")
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