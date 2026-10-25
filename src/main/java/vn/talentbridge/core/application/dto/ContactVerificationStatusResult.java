package vn.talentbridge.core.application.dto;

public record ContactVerificationStatusResult(
        boolean emailVerified,
        boolean phoneVerified,
        String maskedEmail,
        String maskedPhone
) {
}
