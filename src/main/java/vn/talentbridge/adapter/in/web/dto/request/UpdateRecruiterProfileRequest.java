package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRecruiterProfileRequest {

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Schema(example = "Tran Dinh Tinh")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Schema(example = "0901234567")
    private String phone;

    @Size(max = 500, message = "URL ảnh đại diện không được vượt quá 500 ký tự")
    @Schema(example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Size(max = 100, message = "Chức danh không được vượt quá 100 ký tự")
    @Schema(example = "Senior HR Executive")
    private String position;
}