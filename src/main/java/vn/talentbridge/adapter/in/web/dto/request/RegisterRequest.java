package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu đăng ký tài khoản mới (Ứng viên hoặc Nhà tuyển dụng)")
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Địa chỉ email (duy nhất trên toàn hệ thống)", example = "candidate@talentbridge.vn", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
    @Schema(description = "Mật khẩu bảo mật (tối thiểu 6 ký tự)", example = "Password@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Số điện thoại liên hệ (10-11 chữ số)", example = "0987654321")
    private String phone;

    @NotBlank(message = "Vai trò đăng ký không được để trống")
    @Schema(description = "Vai trò người dùng đăng ký", example = "ROLE_CANDIDATE", allowableValues = {
            "ROLE_CANDIDATE", "ROLE_RECRUITER" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    // Hiếu
    // Chức danh tuyển dụng, dùng khi đăng ký tài khoản HR
    @Size(max = 100, message = "Chức danh không được vượt quá 100 ký tự")
    @Schema(description = "Chức danh tuyển dụng, dùng khi đăng ký tài khoản HR", example = "HR Executive")

    private String position;
}
