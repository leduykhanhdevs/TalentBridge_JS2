package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewJoinRequest {

    @NotNull(message = "Trạng thái phê duyệt không được để trống")
    @Schema(description = "Trạng thái phê duyệt yêu cầu", example = "ACCEPTED", allowableValues = {"ACCEPTED", "REJECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private CompanyJoinRequestStatus status;

    @Size(max = 1000, message = "Lý do không được vượt quá 1000 ký tự")
    @Schema(description = "Lý do phê duyệt hoặc từ chối", example = "Chào mừng gia nhập team tuyển dụng!")
    private String reason;
}
