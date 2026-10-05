package vn.talentbridge.adapter.out.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.talentbridge.core.application.dto.ParsedCvResult;
import vn.talentbridge.core.application.port.out.CvParserPort;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@Primary
public class GeminiCvParserService implements CvParserPort {

    private static final String GEMINI_API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final CvParserService fallbackParser;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${talentbridge.gemini.api-key:}")
    private String apiKey;

    @Value("${talentbridge.gemini.model:gemini-1.5-flash}")
    private String model;

    public GeminiCvParserService(
            @Qualifier("cvParserService") CvParserService fallbackParser,
            ObjectMapper objectMapper) {
        this.fallbackParser = fallbackParser;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public ParsedCvResult parse(byte[] bytes, String fileName) {
        // 1. Trích xuất text thô cực nhanh từ file PDF / DOCX thông qua local parser
        String rawText = fallbackParser.extractRawText(bytes, fileName);
        if (rawText == null || rawText.isBlank()) {
            log.warn("[Gemini AI] Không thể trích xuất văn bản thô từ file: {}", fileName);
            return fallbackParser.parse(bytes, fileName);
        }

        // 2. Kiểm tra nếu chưa cấu hình API Key thì dùng fallback rule-based parser
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            log.info("[Gemini AI] Chưa cấu hình API Key (talentbridge.gemini.api-key). Đang sử dụng bộ phân tích Rule-based mặc định.");
            return fallbackParser.parse(bytes, fileName);
        }

        // 3. Gọi Gemini API để bóc tách thông tin với độ chính xác cao
        try {
            log.info("[Gemini AI] Đang gửi nội dung CV ({}, {} ký tự) đến mô hình: {}", fileName, rawText.length(), model);
            return callGemini(rawText, fileName, bytes);
        } catch (Exception e) {
            log.warn("[Gemini AI] Gặp lỗi khi gọi AI ({}: {}). Tự động chuyển đổi sang bộ phân tích Rule-based dự phòng.",
                    e.getClass().getSimpleName(), e.getMessage());
            return fallbackParser.parse(bytes, fileName);
        }
    }

    private ParsedCvResult callGemini(String rawText, String fileName, byte[] originalBytes) throws Exception {
        // Cắt bớt nếu CV quá dài (> 15.000 ký tự) để tối ưu latency và token
        String truncatedText = rawText.length() > 15000 ? rawText.substring(0, 15000) : rawText;

        String systemInstruction = """
                Bạn là một chuyên gia ATS (Applicant Tracking System) hàng đầu về bóc tách và phân tích dữ liệu CV ứng viên.
                Nhiệm vụ của bạn là đọc kỹ toàn bộ văn bản CV được cung cấp và trích xuất thông tin một cách chính xác tuyệt đối, trung thực và đầy đủ theo đúng cấu trúc JSON được yêu cầu.
                
                Quy tắc trích xuất:
                1. Họ và tên (fullName): Tìm tên thật của ứng viên (thường ở đầu CV). Viết hoa đúng chuẩn (Ví dụ: "Nguyễn Văn A"). Không lấy chức danh hay chữ "CV".
                2. Email (email): Địa chỉ email chính xác của ứng viên.
                3. Số điện thoại (phone): Số điện thoại liên hệ dạng 10 số (hoặc bắt đầu bằng +84).
                4. Chức danh (title): Chức danh công việc / vị trí mong muốn (Ví dụ: "Backend Developer", "Kỹ sư Phần mềm", "Fullstack Engineer").
                5. Tỉnh/Thành phố (city): Nơi cư trú hoặc làm việc (Ví dụ: "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", ...).
                6. Tóm tắt (summary): Tóm tắt ngắn gọn 2-3 câu về mục tiêu nghề nghiệp, thế mạnh bản thân.
                7. Kỹ năng (skills): Danh sách tất cả các kỹ năng công nghệ, ngôn ngữ lập trình, framework, công cụ, soft skills được nêu trong CV (Ví dụ: ["Java", "Spring Boot", "React", "Docker", "SQL", ...]).
                8. Kinh nghiệm làm việc (experiences): Danh sách các công việc đã/đang làm (sắp xếp từ mới nhất):
                   - companyName: Tên công ty hoặc doanh nghiệp.
                   - position: Chức danh / vị trí làm việc.
                   - startDate: Ngày/tháng bắt đầu, chuẩn hóa định dạng "YYYY-MM-01" (nếu chỉ có năm thì "YYYY-01-01").
                   - endDate: Ngày/tháng kết thúc, chuẩn hóa định dạng "YYYY-MM-01" (nếu là công việc hiện tại thì để chuỗi rỗng "").
                   - isCurrent: true nếu đang làm việc tại đây (hiện tại / nay / present), ngược lại false.
                   - description: Mô tả trách nhiệm, dự án, công nghệ sử dụng và kết quả đạt được.
                Nếu thông tin nào không có trong CV, hãy để giá trị rỗng ("") hoặc mảng rỗng ([]), tuyệt đối không tự bịa đặt.
                """;

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", "Nội dung CV của ứng viên (File: " + fileName + "):\n\n" + truncatedText)
                        ))
                ),
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", systemInstruction)
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json",
                        "responseSchema", buildResponseSchema()
                )
        );

        String url = String.format(GEMINI_API_URL_TEMPLATE, model, apiKey);

        long startTime = System.currentTimeMillis();
        String responseBody = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(String.class);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Gemini AI] Nhận phản hồi thành công sau {} ms", duration);

        return parseGeminiResponse(responseBody, rawText, fileName, originalBytes);
    }

    private ParsedCvResult parseGeminiResponse(String responseBody, String rawText, String fileName, byte[] originalBytes) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Phản hồi từ Gemini không có text hợp lệ");
        }

        String jsonText = textNode.asText().trim();
        if (jsonText.startsWith("```json")) {
            jsonText = jsonText.substring(7);
        } else if (jsonText.startsWith("```")) {
            jsonText = jsonText.substring(3);
        }
        if (jsonText.endsWith("```")) {
            jsonText = jsonText.substring(0, jsonText.length() - 3);
        }
        jsonText = jsonText.trim();

        JsonNode json = objectMapper.readTree(jsonText);

        String fullName = json.path("fullName").asText("").trim();
        String email = json.path("email").asText("").trim();
        String phone = json.path("phone").asText("").trim();
        String title = json.path("title").asText("").trim();
        String city = json.path("city").asText("").trim();
        String summary = json.path("summary").asText("").trim();

        List<String> skills = new ArrayList<>();
        JsonNode skillsNode = json.path("skills");
        if (skillsNode.isArray()) {
            for (JsonNode s : skillsNode) {
                String sk = s.asText("").trim();
                if (!sk.isBlank() && !skills.contains(sk)) {
                    skills.add(sk);
                }
            }
        }

        List<ParsedCvResult.ParsedExperienceItem> experiences = new ArrayList<>();
        JsonNode expNode = json.path("experiences");
        if (expNode.isArray()) {
            for (JsonNode item : expNode) {
                String companyName = item.path("companyName").asText("").trim();
                String position = item.path("position").asText("").trim();
                String startDate = normalizeDate(item.path("startDate").asText(""));
                String endDate = normalizeDate(item.path("endDate").asText(""));
                boolean isCurrent = item.path("isCurrent").asBoolean(false);
                String description = item.path("description").asText("").trim();

                if (!companyName.isBlank() || !position.isBlank()) {
                    experiences.add(new ParsedCvResult.ParsedExperienceItem(
                            companyName.isBlank() ? "Doanh nghiệp" : companyName,
                            position.isBlank() ? "Chuyên viên" : position,
                            startDate,
                            isCurrent ? null : (endDate.isBlank() ? null : endDate),
                            isCurrent,
                            description
                    ));
                }
            }
        }

        // Tự động bổ sung nếu trường hợp Gemini bỏ sót tên hoặc email (kết hợp với fallback)
        if (fullName.isBlank()) {
            ParsedCvResult fallback = fallbackParser.parse(originalBytes, fileName);
            fullName = fallback.fullName();
        }
        if (email.isBlank()) {
            ParsedCvResult fallback = fallbackParser.parse(originalBytes, fileName);
            email = fallback.email();
        }
        if (phone.isBlank()) {
            ParsedCvResult fallback = fallbackParser.parse(originalBytes, fileName);
            phone = fallback.phone();
        }

        return new ParsedCvResult(
                fullName,
                email,
                phone,
                title,
                city,
                summary,
                skills,
                experiences,
                rawText.length() > 2000 ? rawText.substring(0, 2000) : rawText
        );
    }

    private String normalizeDate(String d) {
        if (d == null || d.isBlank()) return "";
        d = d.trim();
        if (d.matches("^\\d{4}-\\d{2}-\\d{2}$")) return d;
        if (d.matches("^\\d{4}-\\d{2}$")) return d + "-01";
        if (d.matches("^\\d{4}$")) return d + "-01-01";
        if (d.matches("^\\d{2}/\\d{4}$")) {
            String[] parts = d.split("/");
            return parts[1] + "-" + parts[0] + "-01";
        }
        return d;
    }

    private Map<String, Object> buildResponseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "fullName", Map.of("type", "STRING", "description", "Họ và tên của ứng viên"),
                        "email", Map.of("type", "STRING", "description", "Email liên hệ"),
                        "phone", Map.of("type", "STRING", "description", "Số điện thoại di động"),
                        "title", Map.of("type", "STRING", "description", "Chức danh hoặc vị trí ứng tuyển"),
                        "city", Map.of("type", "STRING", "description", "Tỉnh hoặc thành phố sinh sống"),
                        "summary", Map.of("type", "STRING", "description", "Tóm tắt ngắn gọn 2-3 câu"),
                        "skills", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING")
                        ),
                        "experiences", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "companyName", Map.of("type", "STRING"),
                                                "position", Map.of("type", "STRING"),
                                                "startDate", Map.of("type", "STRING"),
                                                "endDate", Map.of("type", "STRING"),
                                                "isCurrent", Map.of("type", "BOOLEAN"),
                                                "description", Map.of("type", "STRING")
                                        ),
                                        "required", List.of("companyName", "position", "startDate", "isCurrent")
                                )
                        )
                ),
                "required", List.of("fullName", "email", "phone", "title", "city", "summary", "skills", "experiences")
        );
    }
}
