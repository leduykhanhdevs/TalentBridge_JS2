package vn.talentbridge.adapter.out.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JavaMailPasswordResetEmailAdapter
        implements PasswordResetEmailPort {

    private static final Logger log = LoggerFactory.getLogger(
            JavaMailPasswordResetEmailAdapter.class
    );

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final boolean mailEnabled;

    public JavaMailPasswordResetEmailAdapter(
            JavaMailSender mailSender,
            @Value("${talentbridge.password-reset.mail-from:no-reply@talentbridge.vn}")
            String mailFrom,
            @Value("${talentbridge.password-reset.mail-enabled:false}")
            boolean mailEnabled
    ) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
    }

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetLink,
            LocalDateTime expiresAt
    ) {
        if (!mailEnabled) {
            log.info(
                    "Password reset email delivery is disabled for {}",
                    recipientEmail
            );
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipientEmail);
        message.setSubject("TalentBridge - Đặt lại mật khẩu");
        message.setText(
                "Bạn đã yêu cầu đặt lại mật khẩu TalentBridge.\n\n"
                        + "Mở liên kết sau để đặt lại mật khẩu:\n"
                        + resetLink
                        + "\n\nLiên kết hết hạn lúc: "
                        + expiresAt.format(DATE_TIME_FORMATTER)
                        + "\n\nNếu bạn không thực hiện yêu cầu này, "
                        + "hãy bỏ qua email."
        );

        mailSender.send(message);
    }
}