package vn.talentbridge.core.application.port.out;

public interface PasswordResetTokenGeneratorPort {

    String generateToken();

    String generateOtp();

    String hashToken(String rawToken);
}
