package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import vn.talentbridge.adapter.in.web.controller.PasswordResetController;
import vn.talentbridge.adapter.in.web.dto.request.ForgotPasswordRequest;
import vn.talentbridge.adapter.in.web.passwordreset.PasswordResetRequestDispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class PasswordResetControllerTest {

    @Test
    void queueSaturationReturnsRetryableResponseWithoutClaimingOtpWasSent() {
        PasswordResetRequestDispatcher dispatcher = mock(PasswordResetRequestDispatcher.class);
        doThrow(new TaskRejectedException("queue full"))
                .when(dispatcher)
                .dispatch("candidate@example.com");

        PasswordResetController controller = new PasswordResetController(dispatcher, null, null);
        var response = controller.forgotPassword(new ForgotPasswordRequest("candidate@example.com"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(50302, response.getBody().getStatusCode());
        assertTrue(response.getBody().getMessage().contains("chưa được xử lý"));
    }
}
