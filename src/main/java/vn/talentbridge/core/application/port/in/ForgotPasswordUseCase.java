package vn.talentbridge.core.application.port.in;

public interface ForgotPasswordUseCase {

    void requestPasswordReset(String email);
}