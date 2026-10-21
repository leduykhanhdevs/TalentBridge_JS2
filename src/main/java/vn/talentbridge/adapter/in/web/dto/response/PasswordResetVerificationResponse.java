package vn.talentbridge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetVerificationResponse(
        @Schema(description = "Mã tạm thời để đặt mật khẩu mới sau khi OTP đã được xác minh")
        String resetToken
) {
}
