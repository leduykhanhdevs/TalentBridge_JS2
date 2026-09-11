package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResult(
    Long id,
    String email,
    String fullName,
    String phoneNumber,
    String avatarUrl,
    String status,
    Set<String> roles,
    LocalDateTime createdAt
) {
    public static UserResult from(User user) {
        return new UserResult(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.getStatus() != null ? user.getStatus().name() : null,
            user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
            user.getCreatedAt()
        );
    }
}