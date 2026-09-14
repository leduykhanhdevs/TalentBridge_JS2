package vn.talentbridge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.AuthResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Phản hồi xác thực chứa Token và thông tin người dùng")
public class AuthResponse {

    @Schema(description = "JWT Access Token dùng cho các request tiếp theo", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjYW5kaWRhdGVAdGFsZW50YnJpZGdlLnZuIi...")
    private String accessToken;

    @Schema(description = "JWT Refresh Token dùng để làm mới Access Token khi hết hạn", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjYW5kaWRhdGVAdGFsZW50YnJpZGdlLnZuIi...")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian hết hạn của Access Token tính theo mili-giây", example = "86400000")
    private long expiresInMs;

    @Schema(description = "Thông tin cơ bản của người dùng sau khi xác thực thành công")
    private UserResponse user;

    public static AuthResponse from(AuthResult result) {
        return AuthResponse.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .tokenType(result.tokenType())
                .expiresInMs(result.expiresIn() * 1000)
                .user(UserResponse.from(result.user()))
                .build();
    }
}