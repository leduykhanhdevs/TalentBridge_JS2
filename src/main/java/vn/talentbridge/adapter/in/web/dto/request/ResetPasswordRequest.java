package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đặt lại mật khẩu")
public class ResetPasswordRequest {

    @NotBlank(message = "Token đặt lại mật khẩu không được để trống")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(
            min = 6,
            message = "Mật khẩu mới phải chứa ít nhất 6 ký tự"
    )
    @Schema(
            example = "NewPassword@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;
}