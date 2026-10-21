package vn.talentbridge.adapter.in.web.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;

@Component
@RequiredArgsConstructor
public class PasswordResetRequestDispatcher {

    private final ForgotPasswordUseCase forgotPasswordUseCase;

    @Async("passwordResetRequestExecutor")
    @Transactional
    public void dispatch(String email) {
        forgotPasswordUseCase.requestPasswordReset(email);
    }
}
