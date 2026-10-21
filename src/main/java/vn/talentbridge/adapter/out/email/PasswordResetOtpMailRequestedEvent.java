package vn.talentbridge.adapter.out.email;

import java.time.LocalDateTime;

public record PasswordResetOtpMailRequestedEvent(
        String recipientEmail,
        String otp,
        LocalDateTime expiresAt
) {
}
