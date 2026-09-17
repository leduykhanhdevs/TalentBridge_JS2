package vn.talentbridge.core.application.port.out;

import java.time.LocalDateTime;

public interface PasswordResetEmailPort {

    void sendPasswordResetEmail(
            String recipientEmail,
            String resetLink,
            LocalDateTime expiresAt
    );
}