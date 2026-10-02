package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResumeRequest {

    @NotBlank(message = "Vui lòng chọn mã mẫu CV")
    private String templateCode;

    @NotBlank(message = "Tiêu đề CV không được để trống")
    private String title;

    private String primaryColor;

    private String customizationJson;

    // Optional rendered HTML content from front-end template builder
    private String htmlContent;
}
