package vn.talentbridge.core.application.port.in;

import java.util.Optional;

public interface VerifyPasswordResetOtpUseCase {

    Optional<String> verifyOtp(String email, String otp);
}
