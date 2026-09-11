package vn.talentbridge.adapter.in.web.dto.response;

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
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private UserStatus status;
    private Set<String> roles;
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