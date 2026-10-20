package vn.talentbridge.core.application.port.out;

import java.time.LocalDateTime;

public interface PasswordResetEmailPort {

    void sendPasswordResetOtp(
            String recipientEmail,
            String otp,
            LocalDateTime expiresAt
    );
}
