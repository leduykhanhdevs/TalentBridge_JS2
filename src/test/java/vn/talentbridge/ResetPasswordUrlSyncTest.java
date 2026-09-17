package vn.talentbridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ResetPasswordUrlSyncTest {

    @Autowired
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Test
    @DisplayName("Default reset password URL must be synchronized to port 5173, not port 3000")
    void defaultResetPasswordUrl_mustBePort5173() {
        assertNotNull(forgotPasswordUseCase);
        Object url = ReflectionTestUtils.getField(forgotPasswordUseCase, "resetPasswordUrl");
        assertEquals("http://localhost:5173/reset-password", url);
    }
}
