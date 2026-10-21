package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import vn.talentbridge.config.MailConfig;

import static org.assertj.core.api.Assertions.assertThat;

class MailConfigTest {

    @Test
    void manuallyCreatedMailSenderAppliesConfiguredSmtpTimeouts() {
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) new MailConfig().javaMailSender(
                "smtp.example.test",
                587,
                "sender@example.test",
                "test-secret",
                1_000,
                2_000,
                3_000
        );

        assertThat(mailSender.getJavaMailProperties())
                .containsEntry("mail.smtp.connectiontimeout", "1000")
                .containsEntry("mail.smtp.timeout", "2000")
                .containsEntry("mail.smtp.writetimeout", "3000");
    }
}
