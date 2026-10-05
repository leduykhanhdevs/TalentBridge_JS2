package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.talentbridge.adapter.out.parser.CvParserService;
import vn.talentbridge.adapter.out.parser.GeminiCvParserService;
import vn.talentbridge.core.application.dto.ParsedCvResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GeminiCvParserServiceTest {

    private final CvParserService mockFallbackParser = mock(CvParserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiCvParserService geminiService = new GeminiCvParserService(mockFallbackParser, objectMapper);

    @Test
    @DisplayName("Tự động fallback về Rule-based parser khi chưa cấu hình API Key")
    void shouldFallbackWhenApiKeyIsBlank() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "");

        byte[] fakeBytes = "fake pdf content".getBytes();
        when(mockFallbackParser.extractRawText(fakeBytes, "test.pdf")).thenReturn("Nội dung CV ứng viên");
        when(mockFallbackParser.parse(fakeBytes, "test.pdf")).thenReturn(
                new ParsedCvResult("Nguyen Van A", "a@test.com", "0901234567", "Dev", "Ha Noi", "", List.of(), List.of(), "")
        );

        ParsedCvResult result = geminiService.parse(fakeBytes, "test.pdf");

        assertThat(result).isNotNull();
        assertThat(result.fullName()).isEqualTo("Nguyen Van A");
        verify(mockFallbackParser, times(1)).parse(fakeBytes, "test.pdf");
    }

    @Test
    @DisplayName("Tự động fallback an toàn khi gặp lỗi kết nối API hoặc quota")
    void shouldFallbackGracefullyOnError() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "INVALID_API_KEY");

        byte[] fakeBytes = "fake pdf content".getBytes();
        when(mockFallbackParser.extractRawText(fakeBytes, "test.pdf")).thenReturn("Nội dung CV ứng viên");
        when(mockFallbackParser.parse(fakeBytes, "test.pdf")).thenReturn(
                new ParsedCvResult("Nguyen Van B", "b@test.com", "0909999999", "Tester", "Da Nang", "", List.of(), List.of(), "")
        );

        ParsedCvResult result = geminiService.parse(fakeBytes, "test.pdf");

        assertThat(result).isNotNull();
        assertThat(result.fullName()).isEqualTo("Nguyen Van B");
        verify(mockFallbackParser, times(1)).parse(fakeBytes, "test.pdf");
    }
}
