package vn.talentbridge.core.application.port.out;

import java.time.LocalDateTime;

public interface ContactVerificationDeliveryPort {

    void sendEmailOtp(String email, String otp, LocalDateTime expiresAt);

    void sendPhoneOtp(String phoneNumber, String otp, LocalDateTime expiresAt);
}
