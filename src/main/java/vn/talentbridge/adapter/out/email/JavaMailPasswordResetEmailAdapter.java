package vn.talentbridge.adapter.out.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;

import java.nio.charset.StandardCharsets;
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
    private final ApplicationEventPublisher eventPublisher;
    private final String mailFrom;
    private final boolean mailEnabled;

    public JavaMailPasswordResetEmailAdapter(
            JavaMailSender mailSender,
            ApplicationEventPublisher eventPublisher,
            @Value("${talentbridge.password-reset.mail-from:no-reply@talentbridge.vn}")
            String mailFrom,
            @Value("${talentbridge.password-reset.mail-enabled:false}")
            boolean mailEnabled
    ) {
        this.mailSender = mailSender;
        this.eventPublisher = eventPublisher;
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
    }

    @Override
    public void sendPasswordResetOtp(
            String recipientEmail,
            String otp,
            LocalDateTime expiresAt
    ) {
        eventPublisher.publishEvent(new PasswordResetOtpMailRequestedEvent(
                recipientEmail,
                otp,
                expiresAt
        ));
    }

    @Async("passwordResetMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPasswordResetOtpAfterCommit(
            PasswordResetOtpMailRequestedEvent event
    ) {
        if (!mailEnabled) {
            throw new PasswordResetMailUnavailableException();
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(mailFrom);
            helper.setTo(event.recipientEmail());
            helper.setSubject("TalentBridge - Yêu cầu đặt lại mật khẩu");

            String formattedExpire = event.expiresAt().format(DATE_TIME_FORMATTER);
            String htmlContent = buildHtmlContent(event.otp(), formattedExpire);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email", e);
            throw new PasswordResetMailUnavailableException();
        }
    }

    private String buildHtmlContent(String otp, String formattedExpire) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"><title>Đặt lại mật khẩu TalentBridge</title></head>"
                + "<body style=\"margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:600px;margin:30px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);\">"
                + "<tr><td style=\"background:#4f46e5;padding:32px;text-align:center;\">"
                + "<h1 style=\"margin:0;color:#ffffff;font-size:24px;font-weight:700;\">TalentBridge ATS</h1>"
                + "</td></tr>"
                + "<tr><td style=\"padding:32px 32px 24px 32px;\">"
                + "<h2 style=\"margin:0 0 16px;color:#0f172a;font-size:20px;\">Mã OTP đặt lại mật khẩu</h2>"
                + "<p style=\"margin:0 0 20px;color:#475569;line-height:1.6;font-size:15px;\">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản TalentBridge của bạn. Nhập mã xác minh này trong ứng dụng để tiếp tục:</p>"
                + "<div style=\"text-align:center;margin:28px 0;padding:20px;background:#eff6ff;border:1px solid #bfdbfe;border-radius:12px;\">"
                + "<span style=\"display:inline-block;color:#1d4ed8;font-size:32px;font-weight:700;letter-spacing:10px;\">" + otp + "</span>"
                + "</div>"
                + "<p style=\"margin:20px 0 0;padding:12px;background:#f8fafc;border-left:4px solid #4f46e5;color:#64748b;font-size:13px;border-radius:4px;\">Mã có hiệu lực đến <strong>" + formattedExpire + "</strong> (15 phút). Không chia sẻ mã này với bất kỳ ai. Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>"
                + "</td></tr>"
                + "<tr><td style=\"background:#f8fafc;padding:20px 32px;text-align:center;color:#94a3b8;font-size:12px;\">"
                + "© 2026 TalentBridge ATS. Hệ thống Quản trị Tuyển dụng Chuyên nghiệp."
                + "</td></tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }
}
