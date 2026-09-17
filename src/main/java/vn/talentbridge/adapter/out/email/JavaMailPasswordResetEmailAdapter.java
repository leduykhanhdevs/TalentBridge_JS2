package vn.talentbridge.adapter.out.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;

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
    @Async
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

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("TalentBridge - Yêu cầu đặt lại mật khẩu");

            String formattedExpire = expiresAt.format(DATE_TIME_FORMATTER);
            String htmlContent = buildHtmlContent(resetLink, formattedExpire);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", recipientEmail, e);
        }
    }

    private String buildHtmlContent(String resetLink, String formattedExpire) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"><title>Đặt lại mật khẩu TalentBridge</title></head>"
                + "<body style=\"margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:600px;margin:30px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);\">"
                + "<tr><td style=\"background:#4f46e5;padding:32px;text-align:center;\">"
                + "<h1 style=\"margin:0;color:#ffffff;font-size:24px;font-weight:700;\">TalentBridge ATS</h1>"
                + "</td></tr>"
                + "<tr><td style=\"padding:32px 32px 24px 32px;\">"
                + "<h2 style=\"margin:0 0 16px;color:#0f172a;font-size:20px;\">Yêu cầu đặt lại mật khẩu</h2>"
                + "<p style=\"margin:0 0 20px;color:#475569;line-height:1.6;font-size:15px;\">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản TalentBridge của bạn. Vui lòng nhấn vào nút bên dưới để tiến hành thiết lập mật khẩu mới:</p>"
                + "<div style=\"text-align:center;margin:28px 0;\">"
                + "<a href=\"" + resetLink + "\" style=\"display:inline-block;background:#4f46e5;color:#ffffff;padding:14px 28px;font-size:15px;font-weight:600;text-decoration:none;border-radius:10px;\">Đặt lại mật khẩu</a>"
                + "</div>"
                + "<p style=\"margin:0 0 12px;color:#64748b;font-size:14px;line-height:1.5;\">Hoặc sao chép đường dẫn sau vào trình duyệt:<br><a href=\"" + resetLink + "\" style=\"color:#4f46e5;word-break:break-all;\">" + resetLink + "</a></p>"
                + "<p style=\"margin:20px 0 0;padding:12px;background:#f8fafc;border-left:4px solid #4f46e5;color:#64748b;font-size:13px;border-radius:4px;\">⏰ Liên kết này có hiệu lực đến <strong>" + formattedExpire + "</strong> (15 phút). Nếu bạn không yêu cầu hành động này, vui lòng bỏ qua email.</p>"
                + "</td></tr>"
                + "<tr><td style=\"background:#f8fafc;padding:20px 32px;text-align:center;color:#94a3b8;font-size:12px;\">"
                + "© 2026 TalentBridge ATS. Hệ thống Quản trị Tuyển dụng Chuyên nghiệp."
                + "</td></tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }
}