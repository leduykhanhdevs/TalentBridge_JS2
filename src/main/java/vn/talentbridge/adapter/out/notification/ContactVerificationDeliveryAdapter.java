package vn.talentbridge.adapter.out.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import vn.talentbridge.core.application.port.out.ContactVerificationDeliveryPort;
import vn.talentbridge.core.domain.exception.ContactVerificationUnavailableException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Component
public class ContactVerificationDeliveryAdapter implements ContactVerificationDeliveryPort {

    private static final DateTimeFormatter EXPIRES_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String APPS_SCRIPT_HOST = "script.google.com";

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String mailFrom;
    private final boolean mailEnabled;
    private final String mailProvider;
    private final URI appsScriptEndpoint;
    private final String appsScriptSecret;
    private final HttpClient httpClient;
    private final String zaloEndpoint;
    private final String zaloAccessToken;
    private final String zaloTemplateId;
    private final String zaloOtpField;

    @Autowired
    public ContactVerificationDeliveryAdapter(
            JavaMailSender mailSender,
            ObjectMapper objectMapper,
            @Value("${talentbridge.contact-verification.mail-from:no-reply@talentbridge.vn}") String mailFrom,
            @Value("${talentbridge.contact-verification.email-enabled:false}") boolean mailEnabled,
            @Value("${talentbridge.contact-verification.mail-provider:smtp}") String mailProvider,
            @Value("${talentbridge.contact-verification.apps-script-url:}") String appsScriptUrl,
            @Value("${talentbridge.contact-verification.apps-script-secret:}") String appsScriptSecret,
            @Value("${talentbridge.contact-verification.zalo-api-url:https://business.openapi.zalo.me/message/template}") String zaloEndpoint,
            @Value("${talentbridge.contact-verification.zalo-access-token:}") String zaloAccessToken,
            @Value("${talentbridge.contact-verification.zalo-template-id:}") String zaloTemplateId,
            @Value("${talentbridge.contact-verification.zalo-otp-field:otp}") String zaloOtpField
    ) {
        this(mailSender, objectMapper, mailFrom, mailEnabled, mailProvider, appsScriptUrl, appsScriptSecret,
                zaloEndpoint, zaloAccessToken, zaloTemplateId, zaloOtpField,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.ALWAYS).build());
    }

    ContactVerificationDeliveryAdapter(
            JavaMailSender mailSender,
            String mailFrom,
            boolean mailEnabled,
            String zaloEndpoint,
            String zaloAccessToken,
            String zaloTemplateId,
            String zaloOtpField
    ) {
        this(mailSender, new ObjectMapper(), mailFrom, mailEnabled, "smtp", "", "",
                zaloEndpoint, zaloAccessToken, zaloTemplateId, zaloOtpField,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.ALWAYS).build());
    }

    ContactVerificationDeliveryAdapter(
            JavaMailSender mailSender,
            ObjectMapper objectMapper,
            String mailFrom,
            boolean mailEnabled,
            String mailProvider,
            String appsScriptUrl,
            String appsScriptSecret,
            String zaloEndpoint,
            String zaloAccessToken,
            String zaloTemplateId,
            String zaloOtpField,
            HttpClient httpClient
    ) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
        this.mailProvider = mailProvider == null ? "" : mailProvider.trim().toLowerCase();
        this.appsScriptSecret = appsScriptSecret == null ? "" : appsScriptSecret;
        this.appsScriptEndpoint = mailEnabled && this.mailProvider.equals("apps-script")
                ? validateAppsScriptEndpoint(appsScriptUrl)
                : null;
        if (mailEnabled && this.mailProvider.equals("apps-script") && this.appsScriptSecret.isBlank()) {
            throw new IllegalStateException("Google Apps Script shared secret is required for contact email OTP");
        }
        if (mailEnabled && !this.mailProvider.equals("apps-script") && !this.mailProvider.equals("smtp")) {
            throw new IllegalStateException("Contact verification mail provider must be smtp or apps-script");
        }
        this.httpClient = httpClient;
        this.zaloEndpoint = zaloEndpoint;
        this.zaloAccessToken = zaloAccessToken;
        this.zaloTemplateId = zaloTemplateId;
        this.zaloOtpField = zaloOtpField;
    }

    @Override
    public void sendEmailOtp(String email, String otp, LocalDateTime expiresAt) {
        if (!mailEnabled) {
            throw new ContactVerificationUnavailableException("Gửi OTP email chưa được cấu hình trên máy chủ.");
        }
        if (mailProvider.equals("apps-script")) {
            sendEmailOtpViaAppsScript(email, otp, expiresAt);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject("TalentBridge - Xác minh email ứng viên");
            helper.setText(buildEmailHtml(otp, expiresAt), true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new ContactVerificationUnavailableException("Không gửi được OTP email. Vui lòng kiểm tra cấu hình email hoặc thử lại sau.", exception);
        }
    }

    private void sendEmailOtpViaAppsScript(String email, String otp, LocalDateTime expiresAt) {
        String expiry = expiresAt.format(EXPIRES_FORMAT);
        String textBody = "Mã xác minh TalentBridge: " + otp + ". Mã có hiệu lực đến " + expiry + ".";
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "secret", appsScriptSecret,
                    "to", email,
                    "subject", "TalentBridge - Xác minh email ứng viên",
                    "textBody", textBody,
                    "htmlBody", buildEmailHtml(otp, expiresAt)
            ));
            HttpRequest request = HttpRequest.newBuilder(appsScriptEndpoint)
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseJson = objectMapper.readTree(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300
                    || responseJson == null || !responseJson.path("ok").asBoolean(false)) {
                throw new ContactVerificationUnavailableException("Không gửi được OTP email. Vui lòng thử lại sau.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ContactVerificationUnavailableException("Gửi OTP email bị gián đoạn. Vui lòng thử lại sau.", exception);
        } catch (ContactVerificationUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ContactVerificationUnavailableException("Không gửi được OTP email. Vui lòng kiểm tra cấu hình relay.", exception);
        }
    }

    private URI validateAppsScriptEndpoint(String endpoint) {
        String value = endpoint == null ? "" : endpoint.trim();
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Google Apps Script URL không hợp lệ.", exception);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !APPS_SCRIPT_HOST.equalsIgnoreCase(uri.getHost())
                || (uri.getPort() != -1 && uri.getPort() != 443)
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || !uri.getPath().matches("^/macros/s/[A-Za-z0-9_-]+/exec$")) {
            throw new IllegalArgumentException("Google Apps Script URL phải là HTTPS /exec endpoint chính thức.");
        }
        return uri;
    }

    @Override
    public void sendPhoneOtp(String phoneNumber, String otp, LocalDateTime expiresAt) {
        if (zaloAccessToken.isBlank() || zaloTemplateId.isBlank()) {
            throw new ContactVerificationUnavailableException("Gửi OTP qua Zalo chưa được cấu hình trên máy chủ.");
        }
        try {
            JsonNode response = restClient.post()
                    .uri(zaloEndpoint)
                    .header("access_token", zaloAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "phone", normalizePhone(phoneNumber),
                            "template_id", zaloTemplateId,
                            "template_data", Map.of(zaloOtpField, otp),
                            "tracking_id", UUID.randomUUID().toString().replace("-", "")
                    ))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.has("error") || response.path("error").asInt(-1) != 0) {
                throw new ContactVerificationUnavailableException("Zalo không chấp nhận yêu cầu gửi OTP.");
            }
        } catch (ContactVerificationUnavailableException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ContactVerificationUnavailableException("Không gửi được OTP qua Zalo. Vui lòng thử lại sau.", exception);
        }
    }

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber == null ? "" : phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("0")) {
            return "84" + digits.substring(1);
        }
        return digits;
    }

    private String buildEmailHtml(String otp, LocalDateTime expiresAt) {
        return "<!doctype html><html lang=\"vi\"><meta charset=\"UTF-8\"><body style=\"font-family:Arial,sans-serif;color:#0f172a\">"
                + "<h2>Xác minh tài khoản TalentBridge</h2>"
                + "<p>Nhập mã dưới đây để xác nhận email của bạn. Mã chỉ dùng một lần.</p>"
                + "<p style=\"font-size:30px;font-weight:bold;letter-spacing:8px;color:#1d4ed8\">" + otp + "</p>"
                + "<p>Mã hết hạn lúc " + expiresAt.format(EXPIRES_FORMAT) + ". Không chia sẻ mã này với bất kỳ ai.</p>"
                + "</body></html>";
    }
}
