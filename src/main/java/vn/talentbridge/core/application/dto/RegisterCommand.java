package vn.talentbridge.core.application.dto;

public record RegisterCommand(
    String email,
    String password,
    String fullName,
    String phoneNumber,
    String role
) {}