package vn.talentbridge.adapter.out.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "talentbridge.password-reset.mail-provider",
        havingValue = "apps-script"
)
public class GoogleAppsScriptPasswordResetEmailAdapter
        implements PasswordResetEmailPort {

    private static final Logger log = LoggerFactory.getLogger(
            GoogleAppsScriptPasswordResetEmailAdapter.class
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SUBJECT =
            "TalentBridge - Mã OTP đặt lại mật khẩu";
    private static final String APPS_SCRIPT_HOST = "script.google.com";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI endpoint;
    private final String sharedSecret;
    private final boolean mailEnabled;

    @Autowired
    public GoogleAppsScriptPasswordResetEmailAdapter(
            ObjectMapper objectMapper,
            @Value("${talentbridge.password-reset.apps-script-url:}")
            String endpoint,
            @Value("${talentbridge.password-reset.apps-script-secret:}")
            String sharedSecret,
            @Value("${talentbridge.password-reset.mail-enabled:false}")
            boolean mailEnabled
    ) {
        this(
                objectMapper,
                endpoint,
                sharedSecret,
                mailEnabled,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
        );
    }

    GoogleAppsScriptPasswordResetEmailAdapter(
            ObjectMapper objectMapper,
            String endpoint,
            String sharedSecret,
            boolean mailEnabled,
            HttpClient httpClient
    ) {
        this.objectMapper = objectMapper;
        this.sharedSecret = sharedSecret;
        this.mailEnabled = mailEnabled;
        this.httpClient = httpClient;

        String endpointValue = endpoint == null ? "" : endpoint.trim();
        if (mailEnabled && (endpointValue.isEmpty()
                || sharedSecret == null || sharedSecret.isBlank())) {
            throw new IllegalStateException(
                    "Google Apps Script mail URL and shared secret are required"
            );
        }
        this.endpoint = endpointValue.isEmpty()
                ? null
                : validateEndpoint(endpointValue);
    }

    @Override
    public void sendPasswordResetOtp(
            String recipientEmail,
            String otp,
            LocalDateTime expiresAt
    ) {
        if (!mailEnabled) {
            throw new PasswordResetMailUnavailableException();
        }

        String expiry = expiresAt.format(DATE_TIME_FORMATTER);
        String htmlBody = buildHtmlBody(otp, expiry);
        String textBody = "Mã OTP đặt lại mật khẩu TalentBridge: " + otp
                + ". Mã có hiệu lực đến " + expiry + ".";

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "secret", sharedSecret,
                    "to", recipientEmail,
                    "subject", SUBJECT,
                    "textBody", textBody,
                    "htmlBody", htmlBody
            ));
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            JsonNode responseJson = objectMapper.readTree(response.body());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300
                    || responseJson == null
                    || !responseJson.path("ok").asBoolean(false)) {
                log.error("Google Apps Script rejected password reset email delivery");
                throw new PasswordResetMailUnavailableException();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Sending password reset email through Apps Script was interrupted");
            throw new PasswordResetMailUnavailableException();
        } catch (PasswordResetMailUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Google Apps Script password reset email delivery failed");
            throw new PasswordResetMailUnavailableException();
        }
    }

    private URI validateEndpoint(String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Google Apps Script mail URL is invalid",
                    exception
            );
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !APPS_SCRIPT_HOST.equalsIgnoreCase(uri.getHost())
                || (uri.getPort() != -1 && uri.getPort() != 443)
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null
                || !uri.getPath().matches("^/macros/s/[A-Za-z0-9_-]+/exec$")) {
            throw new IllegalArgumentException(
                    "Google Apps Script mail URL must be a deployed HTTPS /exec URL"
            );
        }
        return uri;
    }

    private String buildHtmlBody(String otp, String expiry) {
        return "<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">"
                + "<title>Mã OTP TalentBridge</title></head>"
                + "<body style=\"margin:0;padding:24px;background:#f1f5f9;font-family:Arial,sans-serif;\">"
                + "<table role=\"presentation\" style=\"max-width:560px;margin:auto;background:#fff;border-radius:16px;padding:32px;\">"
                + "<tr><td><h1 style=\"color:#1d4ed8;font-size:22px;\">TalentBridge</h1>"
                + "<h2 style=\"color:#0f172a;font-size:19px;\">Mã xác minh đặt lại mật khẩu</h2>"
                + "<p style=\"color:#475569;line-height:1.6;\">Nhập mã OTP dưới đây trên trang TalentBridge để tiếp tục:</p>"
                + "<div style=\"margin:24px 0;padding:18px;text-align:center;background:#eff6ff;border-radius:12px;color:#1d4ed8;font-size:32px;font-weight:bold;letter-spacing:10px;\">"
                + otp
                + "</div><p style=\"color:#64748b;line-height:1.6;\">Mã có hiệu lực đến <strong>"
                + expiry
                + "</strong>. Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>"
                + "</td></tr></table></body></html>";
    }
}
