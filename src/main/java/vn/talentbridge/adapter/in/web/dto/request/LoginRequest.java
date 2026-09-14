package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu đăng nhập tài khoản")
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email đăng nhập", example = "candidate@talentbridge.vn", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu tài khoản (tối thiểu 6 ký tự)", example = "Password@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}