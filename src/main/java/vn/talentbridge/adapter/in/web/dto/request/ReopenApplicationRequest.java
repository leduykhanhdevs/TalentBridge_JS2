package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReopenApplicationRequest(
        @NotBlank(message = "Vui lòng nhập lý do mở lại hồ sơ")
        @Size(max = 1000, message = "Lý do mở lại không được vượt quá 1000 ký tự")
        String reason
) {
}
