package vn.talentbridge.core.application.dto;

public record UpdateRecruiterProfileCommand(
        String fullName,
        String phone,
        String avatarUrl,
        String position) {
}