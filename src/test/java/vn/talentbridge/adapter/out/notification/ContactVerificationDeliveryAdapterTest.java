package vn.talentbridge.adapter.out.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import vn.talentbridge.core.domain.exception.ContactVerificationUnavailableException;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class ContactVerificationDeliveryAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendsNormalizedPhoneAndTemplateDataWithTrackingId() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> accessToken = new AtomicReference<>();
        HttpServer server = startServer(requestBody, accessToken, "{\"error\":0,\"message\":\"Success\"}");
        try {
            ContactVerificationDeliveryAdapter adapter = adapter(server, "qa-zalo-token");
            adapter.sendPhoneOtp("0912345678", "123456", LocalDateTime.now().plusMinutes(10));

            JsonNode body = objectMapper.readTree(requestBody.get());
            assertEquals("84912345678", body.path("phone").asText());
            assertEquals("otp-template", body.path("template_id").asText());
            assertEquals("123456", body.path("template_data").path("otp").asText());
            assertTrue(body.path("tracking_id").asText().matches("[a-f0-9]{32}"));
            assertEquals("qa-zalo-token", accessToken.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsRejectedZaloTemplateResponseToUnavailable() throws Exception {
        HttpServer server = startServer(new AtomicReference<>(), new AtomicReference<>(), "{\"error\":-124,\"message\":\"Template is not enabled\"}");
        try {
            ContactVerificationDeliveryAdapter adapter = adapter(server, "qa-zalo-token");
            assertThrows(ContactVerificationUnavailableException.class,
                    () -> adapter.sendPhoneOtp("0912345678", "123456", LocalDateTime.now().plusMinutes(10)));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void sendsEmailOtpThroughHttpsAppsScriptRelay() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"ok\":true}");
        when(httpClient.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ContactVerificationDeliveryAdapter adapter = appsScriptAdapter(httpClient);
        adapter.sendEmailOtp("candidate@example.test", "123456", LocalDateTime.now().plusMinutes(10));

        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void rejectsAppsScriptRelayFailureInsteadOfClaimingEmailWasSent() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"ok\":false}");
        when(httpClient.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ContactVerificationDeliveryAdapter adapter = appsScriptAdapter(httpClient);
        assertThrows(ContactVerificationUnavailableException.class,
                () -> adapter.sendEmailOtp("candidate@example.test", "123456", LocalDateTime.now().plusMinutes(10)));
    }

    private HttpServer startServer(
            AtomicReference<String> requestBody,
            AtomicReference<String> accessToken,
            String responseBody
    ) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/message/template", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            accessToken.set(exchange.getRequestHeaders().getFirst("access_token"));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        return server;
    }

    private ContactVerificationDeliveryAdapter adapter(HttpServer server, String token) {
        return new ContactVerificationDeliveryAdapter(
                mock(JavaMailSender.class),
                "no-reply@example.test",
                true,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/message/template",
                token,
                "otp-template",
                "otp"
        );
    }

    private ContactVerificationDeliveryAdapter appsScriptAdapter(HttpClient httpClient) {
        return new ContactVerificationDeliveryAdapter(
                mock(JavaMailSender.class),
                objectMapper,
                "no-reply@example.test",
                true,
                "apps-script",
                "https://script.google.com/macros/s/test-script-id/exec",
                "test-shared-secret-not-a-real-credential",
                "https://business.openapi.zalo.me/message/template",
                "",
                "",
                "otp",
                httpClient
        );
    }
}
