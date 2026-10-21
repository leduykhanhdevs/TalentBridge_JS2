package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.talentbridge.adapter.out.email.JavaMailPasswordResetEmailAdapter;
import vn.talentbridge.adapter.out.email.PasswordResetOtpMailRequestedEvent;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JavaMailPasswordResetEmailAdapterTest {

    @Test
    void requestsArePublishedAsOtpMailEventsForPostCommitDelivery() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        JavaMailPasswordResetEmailAdapter adapter = new JavaMailPasswordResetEmailAdapter(
                mailSender,
                publisher,
                "no-reply@example.test",
                true
        );
        LocalDateTime expiresAt = LocalDateTime.of(2026, 10, 22, 12, 30);
        ArgumentCaptor<PasswordResetOtpMailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(PasswordResetOtpMailRequestedEvent.class);

        adapter.sendPasswordResetOtp("candidate@example.test", "123456", expiresAt);

        verify(publisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .isEqualTo(new PasswordResetOtpMailRequestedEvent(
                        "candidate@example.test",
                        "123456",
                        expiresAt
                ));
        Method deliveryMethod = JavaMailPasswordResetEmailAdapter.class.getMethod(
                "sendPasswordResetOtpAfterCommit",
                PasswordResetOtpMailRequestedEvent.class
        );
        assertThat(deliveryMethod.getAnnotation(Async.class).value())
                .isEqualTo("passwordResetMailExecutor");
        assertThat(deliveryMethod.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}
