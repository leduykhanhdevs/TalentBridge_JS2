package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class RequestCreateCompanyRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 200, message = "Tên công ty không được vượt quá 200 ký tự")
    @Schema(example = "Công ty Cổ phần Công nghệ FPT Software", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 50, message = "Mã số thuế không được vượt quá 50 ký tự")
    @Schema(example = "0101234567")
    private String taxCode;

    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "Website phải bắt đầu bằng http:// hoặc https://")
    @Schema(example = "https://fptsoftware.com")
    private String website;

    @Size(max = 50, message = "Quy mô công ty không được vượt quá 50 ký tự")
    @Schema(example = "1000+")
    private String companySize;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    @Schema(example = "Khu công nghệ cao, TP. Thủ Đức")
    private String address;

    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    @Schema(example = "Hồ Chí Minh")
    private String city;

    @Schema(example = "Tập đoàn công nghệ và dịch vụ CNTT hàng đầu")
    private String description;

    @Size(max = 500, message = "URL logo không được vượt quá 500 ký tự")
    @Pattern(regexp = "^$|^https?://.*", message = "URL logo phải bắt đầu bằng http:// hoặc https://")
    @Schema(example = "https://talentbridge.vn/logos/fpt.png")
    private String logoUrl;
}
