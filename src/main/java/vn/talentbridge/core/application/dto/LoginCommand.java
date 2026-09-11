package vn.talentbridge.core.application.dto;

public record LoginCommand(
    String email,
    String password
) {}