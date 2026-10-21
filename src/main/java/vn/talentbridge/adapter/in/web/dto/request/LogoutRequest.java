package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu đăng xuất khỏi phiên hiện tại")
public class LogoutRequest {

    @Schema(description = "Refresh token hiện hành của cùng phiên; token đã bị xoay không thể tự thu hồi phiên")
    private String refreshToken;
}
