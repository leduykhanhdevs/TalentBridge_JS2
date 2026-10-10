package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.ai.GeminiApiClient;
import vn.talentbridge.adapter.out.ai.GroundedTalentBridgeAssistantAdapter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TalentBridgeAssistantAdapterTest {
    private final GroundedTalentBridgeAssistantAdapter adapter = new GroundedTalentBridgeAssistantAdapter(
            new GeminiApiClient("", "https://example.test", 100, 100, null),
            new ObjectMapper(),
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
                configuredProvider, new ObjectMapper(), "gemini-test");

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
}
