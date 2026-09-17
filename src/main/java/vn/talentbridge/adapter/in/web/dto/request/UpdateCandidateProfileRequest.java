package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCandidateProfileRequest {

    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng (VD: 0912345678)")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 500, message = "URL ảnh đại diện không được vượt quá 500 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "URL ảnh đại diện phải bắt đầu bằng http:// hoặc https://")
    private String avatarUrl;

    @Size(max = 150, message = "Chức danh không được vượt quá 150 ký tự")
    private String title;

    private LocalDate dob;

    @Pattern(regexp = "^$|^(?i)(MALE|FEMALE|OTHER|NAM|NU|NỮ|KHAC|KHÁC)$", message = "Giới tính không hợp lệ")
    @Size(max = 10, message = "Giới tính không được vượt quá 10 ký tự")
    private String gender;

    @Size(max = 2000, message = "Tóm tắt bản thân không được vượt quá 2000 ký tự")
    private String summary;

    @Min(value = 0, message = "Số năm kinh nghiệm không được nhỏ hơn 0")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = true, message = "Mức lương hiện tại không được nhỏ hơn 0")
    private BigDecimal currentSalary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Mức lương mong muốn không được nhỏ hơn 0")
    private BigDecimal expectedSalary;

    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    private String city;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "Website cá nhân phải bắt đầu bằng http:// hoặc https://")
    private String personalWebsite;

    @Size(max = 255, message = "LinkedIn URL không được vượt quá 255 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "LinkedIn URL phải bắt đầu bằng http:// hoặc https://")
    private String linkedinUrl;

    @Size(max = 255, message = "GitHub URL không được vượt quá 255 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "GitHub URL phải bắt đầu bằng http:// hoặc https://")
    private String githubUrl;
}
