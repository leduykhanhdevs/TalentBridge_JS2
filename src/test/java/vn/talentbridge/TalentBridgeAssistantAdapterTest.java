package vn.talentbridge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.ai.GeminiApiClient;
import vn.talentbridge.adapter.out.ai.GroundedTalentBridgeAssistantAdapter;
import vn.talentbridge.core.application.dto.AssistantConversationTurn;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TalentBridgeAssistantAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GroundedTalentBridgeAssistantAdapter adapter = new GroundedTalentBridgeAssistantAdapter(
            new GeminiApiClient("", "https://example.test", 100, 100, null),
            objectMapper,
            "gemini-test");

    @Test
    void answersApplicationQuestionsFromCuratedKnowledgeWhenProviderIsNotConfigured() {
        var result = adapter.answer("Làm sao để ứng tuyển việc làm trên TalentBridge?", List.of());

        assertThat(result.source()).isEqualTo("KNOWLEDGE_BASE");
        assertThat(result.references()).isNotEmpty();
        assertThat(result.answer()).contains("Ứng viên vào mục Việc làm");
    }

    @Test
    void keepsQuestionsOutsideTalentBridgeScopeOutOfProviderAndKnowledgeBase() {
        var result = adapter.answer("Hãy cho tôi biết kết quả bóng đá hôm nay", List.of());

        assertThat(result.source()).isEqualTo("KNOWLEDGE_BASE");
        assertThat(result.references()).isEmpty();
        assertThat(result.answer()).contains("chỉ hỗ trợ các câu hỏi");
    }

    @Test
    void answersStarterQuestionsFromLocalKnowledgeWithoutWaitingForGemini() {
        GeminiApiClient configuredProvider = new GeminiApiClient(
                "test-key", "https://example.test", 100, 100, null) {
            @Override
            public String generateContent(String model, Object requestPayload) {
                throw new AssertionError("Starter FAQs must not call the external AI provider");
            }
        };
        var fastAdapter = new GroundedTalentBridgeAssistantAdapter(
                configuredProvider, objectMapper, "gemini-test");

        Map<String, String> expectedContent = Map.of(
                "Làm sao để ứng tuyển?", "Ứng viên vào mục Việc làm",
                "Tạo tin tuyển dụng thế nào?", "Nhà tuyển dụng cập nhật hồ sơ HR",
                "Quên mật khẩu phải làm gì?", "Đăng nhập bằng email và mật khẩu"
        );

        expectedContent.forEach((question, expectedAnswer) -> {
            var result = fastAdapter.answer(question, List.of());
            assertThat(result.source()).isEqualTo("KNOWLEDGE_BASE");
            assertThat(result.answer()).contains(expectedAnswer);
            assertThat(result.references()).isNotEmpty();
        });
    }

    @Test
    void generatesAnAnswerFromRetrievedEvidenceWithoutSendingHistoryOrDeprecatedSamplingSettings() throws Exception {
        AtomicReference<Map<String, Object>> capturedRequest = new AtomicReference<>();
        GeminiApiClient configuredProvider = new GeminiApiClient(
                "test-key", "https://example.test", 100, 100, null) {
            @Override
            @SuppressWarnings("unchecked")
            public String generateContent(String model, Object requestPayload) {
                assertThat(model).isEqualTo("gemini-test");
                capturedRequest.set(objectMapper.convertValue(
                        requestPayload, new TypeReference<Map<String, Object>>() { }));
                return """
                        {"candidates":[{"content":{"parts":[{"text":"Bạn có thể mở mục Việc làm để tìm và lọc cơ hội."}]}}]}
                        """;
            }
        };
        var groundedAdapter = new GroundedTalentBridgeAssistantAdapter(
                configuredProvider, objectMapper, "gemini-test");
        String privateHistory = "Tài khoản của tôi là khanh@example.com, mã OTP 123456.";

        var result = groundedAdapter.answer(
                "Trong TalentBridge tôi tìm và lọc công việc ở mục nào?",
                List.of(new AssistantConversationTurn("USER", privateHistory)));

        assertThat(result.source()).isEqualTo("GEMINI");
        assertThat(result.answer()).contains("mục Việc làm");
        assertThat(result.references()).contains("Tìm việc và ứng tuyển");

        String serializedRequest = objectMapper.writeValueAsString(capturedRequest.get());
        assertThat(serializedRequest).contains("mục nào", "Tìm việc và ứng tuyển");
        assertThat(serializedRequest).doesNotContain("khanh@example.com", "123456");
        assertThat(capturedRequest.get()).containsKeys("systemInstruction", "contents", "generationConfig");
        assertThat(objectMapper.writeValueAsString(capturedRequest.get().get("systemInstruction")))
                .contains("tài liệu tham chiếu");
        Map<String, Object> generationConfig = objectMapper.convertValue(
                capturedRequest.get().get("generationConfig"), new TypeReference<Map<String, Object>>() { });
        assertThat(generationConfig).containsEntry("maxOutputTokens", 400);
        assertThat(generationConfig).doesNotContainKey("temperature");
    }

    @Test
    void blocksPersonalDataAndSecretsBeforeCallingTheExternalProvider() {
        AtomicInteger calls = new AtomicInteger();
        GeminiApiClient configuredProvider = new GeminiApiClient(
                "test-key", "https://example.test", 100, 100, null) {
            @Override
            public String generateContent(String model, Object requestPayload) {
                calls.incrementAndGet();
                return "{}";
            }
        };
        var guardedAdapter = new GroundedTalentBridgeAssistantAdapter(
                configuredProvider, objectMapper, "gemini-test");

        for (String message : List.of(
                "Tài khoản TalentBridge của tôi là khanh@example.com, giúp tôi tìm việc",
                "Số điện thoại 0912345678 của tôi nằm trong hồ sơ ứng tuyển",
                "Mã OTP 123456 dùng cho tài khoản TalentBridge",
                "Mật khẩu là demo-secret-1234, tôi cần cập nhật tài khoản TalentBridge",
                "API key: AIzaSyDUMMY_KEY_VALUE_12345678901234567890, dùng cho TalentBridge")) {
            var result = guardedAdapter.answer(message, List.of());

            assertThat(result.source()).isEqualTo("KNOWLEDGE_BASE");
            assertThat(result.references()).isEmpty();
            assertThat(result.answer()).contains("không xử lý thông tin cá nhân");
        }
        assertThat(calls).hasValue(0);
    }

    @Test
    void exposesTheSameGroundedKnowledgeToTheMcpSearchPort() {
        var results = adapter.search("How do I apply for a job on TalentBridge?", 3);

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().title()).isEqualTo("Tìm việc và ứng tuyển");
        assertThat(results.getFirst().answer()).contains("mục Việc làm");
    }
}
