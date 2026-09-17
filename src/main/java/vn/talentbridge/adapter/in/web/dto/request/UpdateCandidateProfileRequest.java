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
    private String phone;

    private String avatarUrl;

    @Size(max = 150, message = "Chức danh không được vượt quá 150 ký tự")
    private String title;

    private LocalDate dob;

    private String gender;

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

    private String personalWebsite;

    private String linkedinUrl;

    private String githubUrl;
}
