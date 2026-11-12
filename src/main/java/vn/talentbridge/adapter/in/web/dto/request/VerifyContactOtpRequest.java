package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyContactOtpRequest {

    @NotBlank(message = "Mã xác minh không được để trống.")
    @Pattern(regexp = "\\d{6}", message = "Mã xác minh phải gồm 6 chữ số.")
    private String otp;
}
