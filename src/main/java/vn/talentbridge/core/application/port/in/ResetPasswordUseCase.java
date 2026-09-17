package vn.talentbridge.core.application.port.in;

public interface ResetPasswordUseCase {

    void resetPassword(
            String rawToken,
            String newPassword
    );
}