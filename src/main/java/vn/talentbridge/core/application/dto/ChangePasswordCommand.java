package vn.talentbridge.core.application.dto;

public record ChangePasswordCommand(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
