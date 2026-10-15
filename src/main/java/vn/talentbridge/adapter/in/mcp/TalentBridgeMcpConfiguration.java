package vn.talentbridge.adapter.in.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.McpCandidateProfileResult;
import vn.talentbridge.core.application.dto.McpJobSummary;
import vn.talentbridge.core.application.dto.TalentBridgeKnowledgeEntry;
import vn.talentbridge.core.application.port.in.SearchTalentBridgeKnowledgeUseCase;
import vn.talentbridge.core.application.port.in.TalentBridgeMcpUseCase;
import vn.talentbridge.core.domain.exception.DomainException;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class TalentBridgeMcpConfiguration {
    private static final int MAX_SEARCH_LIMIT = 20;

    @Bean
    McpJsonMapper mcpJsonMapper(ObjectMapper objectMapper) {
        return new JacksonMcpJsonMapper(objectMapper);
    }

    @Bean
    HttpServletStreamableServerTransportProvider talentBridgeMcpTransportProvider(McpJsonMapper jsonMapper) {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint("/api/v1/mcp")
                .contextExtractor(this::extractRecruiterContext)
                .build();
    }

    @Bean
    ServletRegistrationBean<HttpServletStreamableServerTransportProvider> talentBridgeMcpServlet(
            HttpServletStreamableServerTransportProvider transportProvider) {
        ServletRegistrationBean<HttpServletStreamableServerTransportProvider> registration =
                new ServletRegistrationBean<>(transportProvider, "/api/v1/mcp");
        registration.setName("talentBridgeMcpServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean(destroyMethod = "close")
    McpSyncServer talentBridgeMcpServer(
            HttpServletStreamableServerTransportProvider transportProvider,
            TalentBridgeMcpUseCase mcpUseCase,
            SearchTalentBridgeKnowledgeUseCase knowledgeUseCase,
            ObjectMapper objectMapper) {
        return McpServer.sync(transportProvider)
                .serverInfo("TalentBridge ATS Tools", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .toolCall(tool("talentbridge_search_jobs", "Tìm tin ACTIVE theo từ khóa hoặc thành phố.", objectSchema(Map.of(
                        "keyword", stringSchema("Từ khóa tùy chọn"),
                        "city", stringSchema("Thành phố tùy chọn"),
                        "limit", integerSchema("Số lượng kết quả (1-20), mặc định 5")), List.of())),
                        (exchange, request) -> invoke(exchange, () -> {
                            Map<String, Object> args = request.arguments();
                            String keyword = optionalText(args, "keyword");
                            String city = optionalText(args, "city");
                            int limit = optionalInteger(args, "limit", 5);
                            if (limit < 1 || limit > MAX_SEARCH_LIMIT) {
                                throw new IllegalArgumentException("limit phải từ 1 đến 20.");
                            }
                            List<McpJobSummary> jobs = mcpUseCase.searchJobs(
                                    recruiterId(exchange), keyword, city, limit);
                            return json(objectMapper, jobs);
                        }))
                .toolCall(tool("talentbridge_get_candidate_profile", "Xem hồ sơ tối thiểu của ứng viên đã nộp vào tin thuộc công ty của bạn.", objectSchema(Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "candidateId", integerSchema("Mã ứng viên")), List.of("jobId", "candidateId"))),
                        (exchange, request) -> invoke(exchange, () -> {
                            Map<String, Object> args = request.arguments();
                            McpCandidateProfileResult profile = mcpUseCase.getCandidateProfile(
                                    recruiterId(exchange), requiredLong(args, "jobId"), requiredLong(args, "candidateId"));
                            return json(objectMapper, profile);
                        }))
                .toolCall(tool("talentbridge_evaluate_cv_fit", "Đánh giá tư vấn ứng viên đã nộp vào tin tuyển dụng của bạn.", objectSchema(Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "candidateId", integerSchema("Mã ứng viên")), List.of("jobId", "candidateId"))),
                        (exchange, request) -> invoke(exchange, () -> {
                            Map<String, Object> args = request.arguments();
                            CandidateJobMatchResult result = mcpUseCase.evaluateCandidate(
                                    recruiterId(exchange), requiredLong(args, "jobId"), requiredLong(args, "candidateId"));
                            return json(objectMapper, result);
                        }))
                .toolCall(tool("talentbridge_update_application_stage", "Chuyển ứng viên sang vòng tuyển dụng hợp lệ và lưu lịch sử.", objectSchema(Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "applicationId", integerSchema("Mã đơn ứng tuyển"),
                        "stage", stringSchema("Giai đoạn mới"),
                        "note", stringSchema("Ghi chú tùy chọn")), List.of("jobId", "applicationId", "stage"))),
                        (exchange, request) -> invoke(exchange, () -> {
                            Map<String, Object> args = request.arguments();
                            JobApplicantResult result = mcpUseCase.updateApplicationStage(
                                    recruiterId(exchange), requiredLong(args, "jobId"),
                                    requiredLong(args, "applicationId"), requiredText(args, "stage"),
                                    optionalText(args, "note"));
                            return json(objectMapper, result);
                        }))
                .toolCall(tool("talentbridge_search_help", "Tìm hướng dẫn đã được kiểm duyệt về quy trình và chức năng TalentBridge. Chỉ đọc tài liệu hướng dẫn; không truy cập dữ liệu riêng.", objectSchema(Map.of(
                        "query", stringSchema("Câu hỏi về cách sử dụng TalentBridge"),
                        "limit", integerSchema("Số kết quả (1-5), mặc định 3")), List.of("query"))),
                        (exchange, request) -> invoke(exchange, () -> {
                            String query = requiredText(request.arguments(), "query");
                            int limit = optionalInteger(request.arguments(), "limit", 3);
                            List<TalentBridgeKnowledgeEntry> entries = knowledgeUseCase.search(query, limit);
                            return json(objectMapper, entries);
                        }))
                .build();
    }

    private McpTransportContext extractRecruiterContext(HttpServletRequest ignoredRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                || principal.getId() == null
                || principal.getAuthorities().stream().noneMatch(authority -> "ROLE_RECRUITER".equals(authority.getAuthority()))) {
            return McpTransportContext.EMPTY;
        }
        return McpTransportContext.create(Map.of("userId", principal.getId(), "role", "RECRUITER"));
    }

    private McpSchema.Tool tool(String name, String description, Map<String, Object> schema) {
        return McpSchema.Tool.builder(name, schema).description(description).build();
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        return Map.of("type", "object", "properties", properties, "required", required,
                "additionalProperties", false);
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> integerSchema(String description) {
        return Map.of("type", "integer", "description", description);
    }

    private String json(ObjectMapper objectMapper, Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private McpSchema.CallToolResult invoke(McpSyncServerExchange exchange, ToolAction action) {
        try {
            recruiterId(exchange);
            return textResult(action.execute(), false);
        } catch (IllegalArgumentException | DomainException exception) {
            return textResult(exception.getMessage(), true);
        } catch (Exception exception) {
            log.error("TalentBridge MCP tool failed ({})", exception.getClass().getSimpleName());
            return textResult("Không thể hoàn tất thao tác TalentBridge lúc này.", true);
        }
    }

    private Long recruiterId(McpSyncServerExchange exchange) {
        Object role = exchange.transportContext().get("role");
        Object userId = exchange.transportContext().get("userId");
        if (!"RECRUITER".equals(role) || !(userId instanceof Number number) || number.longValue() <= 0) {
            throw new IllegalArgumentException("Yêu cầu phiên đăng nhập nhà tuyển dụng hợp lệ.");
        }
        return number.longValue();
    }

    private String requiredText(Map<String, Object> arguments, String field) {
        String value = optionalText(arguments, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " là bắt buộc.");
        return value;
    }

    private String optionalText(Map<String, Object> arguments, String field) {
        Object value = arguments.get(field);
        if (value == null) return "";
        if (!(value instanceof String text)) throw new IllegalArgumentException(field + " phải là chuỗi.");
        return text.trim();
    }

    private Long requiredLong(Map<String, Object> arguments, String field) {
        Object value = arguments.get(field);
        if (!(value instanceof Number number) || number.doubleValue() != number.longValue() || number.longValue() <= 0) {
            throw new IllegalArgumentException(field + " phải là số nguyên dương.");
        }
        return number.longValue();
    }

    private int optionalInteger(Map<String, Object> arguments, String field, int defaultValue) {
        Object value = arguments.get(field);
        if (value == null) return defaultValue;
        if (!(value instanceof Number number) || number.doubleValue() != number.intValue()) {
            throw new IllegalArgumentException(field + " phải là số nguyên.");
        }
        return number.intValue();
    }

    private McpSchema.CallToolResult textResult(String text, boolean isError) {
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(text == null ? "Thao tác không thành công." : text)))
                .isError(isError)
                .build();
    }

    @FunctionalInterface
    private interface ToolAction {
        String execute() throws Exception;
    }
}
