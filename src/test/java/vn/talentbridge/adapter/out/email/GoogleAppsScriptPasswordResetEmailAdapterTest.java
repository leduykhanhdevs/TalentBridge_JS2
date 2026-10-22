package vn.talentbridge.adapter.out.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleAppsScriptPasswordResetEmailAdapterTest {

    private static final String ENDPOINT =
            "https://script.google.com/macros/s/test_deployment_123/exec";
    private static final String SECRET = "test-secret-not-for-production";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = mock(HttpClient.class);
    private final GoogleAppsScriptPasswordResetEmailAdapter adapter =
            new GoogleAppsScriptPasswordResetEmailAdapter(
                    objectMapper,
                    ENDPOINT,
                    SECRET,
                    true,
                    httpClient
            );

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void postsOtpToConfiguredRelayAndAcceptsSuccessfulResponse() throws Exception {
        HttpResponse<String> response = response(200, "{\"ok\":true}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        adapter.sendPasswordResetOtp(
                "candidate@example.com",
                "123456",
                LocalDateTime.of(2026, 10, 22, 18, 0)
        );

        org.mockito.ArgumentCaptor<HttpRequest> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(
                requestCaptor.capture(),
                any(HttpResponse.BodyHandler.class)
        );
        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri()).isEqualTo(java.net.URI.create(ENDPOINT));
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.headers().firstValue("Content-Type"))
                .contains("application/json; charset=UTF-8");

        JsonNode payload = objectMapper.readTree(readBody(request));
        assertThat(payload.path("secret").asText()).isEqualTo(SECRET);
        assertThat(payload.path("to").asText()).isEqualTo("candidate@example.com");
        assertThat(payload.path("subject").asText())
                .isEqualTo("TalentBridge - Mã OTP đặt lại mật khẩu");
        assertThat(payload.path("textBody").asText()).contains("123456");
        assertThat(payload.path("htmlBody").asText()).contains("123456");
    }

    @Test
    void failsClosedWhenRelayRejectsDelivery() throws Exception {
        HttpResponse<String> response = response(200, "{\"ok\":false}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        assertThatThrownBy(() -> adapter.sendPasswordResetOtp(
                "candidate@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(15)
        )).isInstanceOf(PasswordResetMailUnavailableException.class);
    }

    @Test
    void failsClosedWhenRelayReturnsAnHttpError() throws Exception {
        HttpResponse<String> response = response(503, "{\"ok\":false}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        assertThatThrownBy(() -> adapter.sendPasswordResetOtp(
                "candidate@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(15)
        )).isInstanceOf(PasswordResetMailUnavailableException.class);
    }

    @Test
    void restoresInterruptFlagAndFailsClosedWhenRequestIsInterrupted()
            throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException());

        assertThatThrownBy(() -> adapter.sendPasswordResetOtp(
                "candidate@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(15)
        )).isInstanceOf(PasswordResetMailUnavailableException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    private HttpResponse<String> response(int status, String body) {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }

    private String readBody(HttpRequest request) throws Exception {
        CompletableFuture<String> result = new CompletableFuture<>();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        request.bodyPublisher().orElseThrow().subscribe(
                new Flow.Subscriber<>() {
                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(ByteBuffer buffer) {
                        byte[] chunk = new byte[buffer.remaining()];
                        buffer.get(chunk);
                        bytes.writeBytes(chunk);
                    }

                    @Override
                    public void onError(Throwable failure) {
                        result.completeExceptionally(failure);
                    }

                    @Override
                    public void onComplete() {
                        result.complete(bytes.toString(StandardCharsets.UTF_8));
                    }
                }
        );
        return result.get();
    }
}
