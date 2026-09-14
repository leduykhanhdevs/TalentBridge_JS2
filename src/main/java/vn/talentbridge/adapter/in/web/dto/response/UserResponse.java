package vn.talentbridge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin chi tiết tài khoản người dùng")
public class UserResponse {

    @Schema(description = "ID định danh người dùng duy nhất", example = "1")
    private Long id;

    @Schema(description = "Email tài khoản", example = "candidate@talentbridge.vn")
    private String email;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://talentbridge.vn/avatars/user-1.jpg")
    private String avatarUrl;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Danh sách vai trò quyền hạn", example = "[\"ROLE_CANDIDATE\"]")
    private Set<String> roles;

    @Schema(description = "Thời điểm khởi tạo tài khoản", example = "2026-09-14T10:00:00")
    private LocalDateTime createdAt;

    public static UserResponse from(UserResult result) {
        return UserResponse.builder()
                .id(result.id())
                .email(result.email())
                .fullName(result.fullName())
                .phone(result.phoneNumber())
                .avatarUrl(result.avatarUrl())
                .status(result.status() != null ? UserStatus.valueOf(result.status()) : null)
                .roles(result.roles())
                .createdAt(result.createdAt())
                .build();
    }

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}