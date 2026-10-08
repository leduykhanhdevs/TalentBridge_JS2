package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.ai.GeminiApiClient;
import vn.talentbridge.adapter.out.parser.CvParserService;
import vn.talentbridge.adapter.out.parser.GeminiCvParserService;
import vn.talentbridge.core.application.dto.CvParsingSource;
import vn.talentbridge.core.application.dto.ParsedCvResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GeminiCvParserServiceTest {

    private final CvParserService fallbackParser = mock(CvParserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    @DisplayName("Đọc JSON Gemini kể cả khi nhà cung cấp trả Content-Type application/octet-stream")
    void parsesOctetStreamResponseAndSendsApiKeyInHeader() throws Exception {
        AtomicReference<String> receivedApiKey = new AtomicReference<>();
        AtomicReference<String> receivedRequest = new AtomicReference<>();
        String modelJson = objectMapper.writeValueAsString(Map.of(
                "fullName", "Nguyen Van A",
                "email", "a@example.com",
                "phone", "0901234567",
                "title", "Backend Developer",
                "city", "Ha Noi",
                "summary", "Experienced backend engineer",
                "skills", List.of("Java", "Spring Boot"),
                "experiences", List.of()
        ));
        String responseJson = objectMapper.writeValueAsString(Map.of(
                "candidates", List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", modelJson)))))
        ));
        startServer(200, responseJson, receivedApiKey, receivedRequest);

        byte[] cvBytes = "cv contents".getBytes(StandardCharsets.UTF_8);
        when(fallbackParser.extractRawText(cvBytes, "private-cv.pdf")).thenReturn("CV content without personal details");
        GeminiCvParserService service = service("test-secret");

        ParsedCvResult result = service.parse(cvBytes, "private-cv.pdf");

        assertThat(result.fullName()).isEqualTo("Nguyen Van A");
        assertThat(result.processingSource()).isEqualTo(CvParsingSource.GEMINI);
        assertThat(receivedApiKey.get()).isEqualTo("test-secret");
        assertThat(receivedRequest.get()).doesNotContain("private-cv.pdf");
        verify(fallbackParser, never()).parse(cvBytes, "private-cv.pdf");
    }

    @Test
    @DisplayName("Dùng parser rule-based khi chưa cấu hình API key")
    void fallsBackWhenApiKeyIsBlank() {
        byte[] cvBytes = "cv contents".getBytes(StandardCharsets.UTF_8);
        when(fallbackParser.extractRawText(cvBytes, "test.pdf")).thenReturn("Nội dung CV ứng viên");
        ParsedCvResult fallback = ruleBasedResult("Nguyen Van A", "a@test.com");
        when(fallbackParser.parse(cvBytes, "test.pdf")).thenReturn(fallback);
        GeminiCvParserService service = service("");

        ParsedCvResult result = service.parse(cvBytes, "test.pdf");

        assertThat(result.fullName()).isEqualTo("Nguyen Van A");
        assertThat(result.processingSource()).isEqualTo(CvParsingSource.RULE_BASED);
        verify(fallbackParser).parse(cvBytes, "test.pdf");
    }

    @Test
    @DisplayName("Fallback có nguồn gốc rõ ràng khi Gemini trả lỗi HTTP")
    void fallsBackGracefullyOnHttpError() throws Exception {
        AtomicReference<String> receivedApiKey = new AtomicReference<>();
        AtomicReference<String> receivedRequest = new AtomicReference<>();
        startServer(403, "upstream error", receivedApiKey, receivedRequest);

        byte[] cvBytes = "cv contents".getBytes(StandardCharsets.UTF_8);
        when(fallbackParser.extractRawText(cvBytes, "test.pdf")).thenReturn("Nội dung CV ứng viên");
        when(fallbackParser.parse(cvBytes, "test.pdf")).thenReturn(ruleBasedResult("Nguyen Van B", "b@test.com"));
        GeminiCvParserService service = service("test-secret");

        ParsedCvResult result = service.parse(cvBytes, "test.pdf");

        assertThat(result.fullName()).isEqualTo("Nguyen Van B");
        assertThat(result.processingSource()).isEqualTo(CvParsingSource.RULE_BASED);
    }

    private GeminiCvParserService service(String apiKey) {
        GeminiApiClient client = new GeminiApiClient(apiKey, baseUrl(), 5000, 5000, null);
        GeminiCvParserService service = new GeminiCvParserService(fallbackParser, objectMapper, client);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "model", "test-model");
        return service;
    }

    private void startServer(
            int status,
            String body,
            AtomicReference<String> receivedApiKey,
            AtomicReference<String> receivedRequest
    ) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1beta/models/test-model:generateContent", exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            receivedRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
    }

    private String baseUrl() {
        if (server == null) return "http://127.0.0.1:1/v1beta";
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta";
    }

    private ParsedCvResult ruleBasedResult(String name, String email) {
        return new ParsedCvResult(name, email, "0901234567", "Developer", "Ha Noi", "", List.of(), List.of(), "");
    }
}
