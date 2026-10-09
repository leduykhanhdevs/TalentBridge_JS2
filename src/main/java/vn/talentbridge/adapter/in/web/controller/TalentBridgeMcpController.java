package vn.talentbridge.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.McpCandidateProfileResult;
import vn.talentbridge.core.application.dto.McpJobSummary;
import vn.talentbridge.core.application.port.in.TalentBridgeMcpUseCase;
import vn.talentbridge.core.domain.exception.DomainException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/mcp")
@PreAuthorize("hasRole('RECRUITER')")
@RequiredArgsConstructor
public class TalentBridgeMcpController {
    private static final int MAX_SEARCH_LIMIT = 20;

    private final TalentBridgeMcpUseCase mcpUseCase;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMcpServerInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "TalentBridge ATS Tools",
                "version", "1.0.0",
                "jsonRpcVersion", "2.0",
                "description", "Custom TalentBridge JSON-RPC tool endpoint for authenticated recruiters; not a full MCP server.",
                "endpoints", Map.of("jsonRpc", "/api/v1/mcp", "supportedMethods", List.of("tools/list", "tools/call", "ping")),
                "toolsAvailable", List.of(
                        "talentbridge_search_jobs",
                        "talentbridge_get_candidate_profile",
                        "talentbridge_evaluate_cv_fit",
                        "talentbridge_update_application_stage"
                )
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleJsonRpc(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody JsonNode request) {
        Object id = jsonRpcId(request);
        if (request == null || !request.isObject() || !"2.0".equals(request.path("jsonrpc").asText())) {
            return ResponseEntity.ok(errorResponse(id, -32600, "Invalid Request"));
        }

        String method = request.path("method").asText("");
        if (method.isBlank()) {
            return ResponseEntity.ok(errorResponse(id, -32600, "Invalid Request: Missing method"));
        }

        try {
            return switch (method) {
                case "ping" -> ResponseEntity.ok(successResponse(id, Map.of("status", "pong")));
                case "tools/list" -> ResponseEntity.ok(successResponse(id, Map.of("tools", availableTools())));
                case "tools/call" -> ResponseEntity.ok(successResponse(id, callTool(principal, request.path("params"))));
                default -> ResponseEntity.ok(errorResponse(id, -32601, "Method not found"));
            };
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.ok(errorResponse(id, -32602, "Invalid params: " + exception.getMessage()));
        } catch (DomainException exception) {
            return ResponseEntity.ok(errorResponse(id, -32000, exception.getMessage()));
        } catch (Exception exception) {
            log.error("MCP request failed for method {} ({})", method, exception.getClass().getSimpleName());
            return ResponseEntity.ok(errorResponse(id, -32603, "Internal error"));
        }
    }

    private Map<String, Object> callTool(UserPrincipal principal, JsonNode params) throws Exception {
        if (params == null || !params.isObject()) {
            throw new IllegalArgumentException("Missing params object");
        }
        String name = requiredText(params, "name");
        JsonNode arguments = params.path("arguments");
        if (!arguments.isMissingNode() && !arguments.isObject()) {
            throw new IllegalArgumentException("arguments must be an object");
        }
        if (arguments.isMissingNode()) {
            arguments = objectMapper.createObjectNode();
        }

        Object result = switch (name) {
            case "talentbridge_search_jobs" -> searchJobs(principal, arguments);
            case "talentbridge_get_candidate_profile" -> getCandidateProfile(principal, arguments);
            case "talentbridge_evaluate_cv_fit" -> evaluateCandidate(principal, arguments);
            case "talentbridge_update_application_stage" -> updateApplicationStage(principal, arguments);
            default -> throw new IllegalArgumentException("Unknown TalentBridge tool");
        };
        return contentResponse(objectMapper.writeValueAsString(result));
    }

    private List<McpJobSummary> searchJobs(UserPrincipal principal, JsonNode arguments) {
        String keyword = optionalText(arguments, "keyword");
        String city = optionalText(arguments, "city");
        int limit = optionalInteger(arguments, "limit", 5);
        if (limit < 1 || limit > MAX_SEARCH_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and 20");
        }
        return mcpUseCase.searchJobs(principal.getId(), keyword, city, limit);
    }

    private McpCandidateProfileResult getCandidateProfile(UserPrincipal principal, JsonNode arguments) {
        return mcpUseCase.getCandidateProfile(principal.getId(),
                requiredLong(arguments, "jobId"), requiredLong(arguments, "candidateId"));
    }

    private CandidateJobMatchResult evaluateCandidate(UserPrincipal principal, JsonNode arguments) {
        return mcpUseCase.evaluateCandidate(principal.getId(),
                requiredLong(arguments, "jobId"), requiredLong(arguments, "candidateId"));
    }

    private JobApplicantResult updateApplicationStage(UserPrincipal principal, JsonNode arguments) {
        return mcpUseCase.updateApplicationStage(principal.getId(),
                requiredLong(arguments, "jobId"),
                requiredLong(arguments, "applicationId"),
                requiredText(arguments, "stage"),
                optionalText(arguments, "note"));
    }

    private List<Map<String, Object>> availableTools() {
        return List.of(
                tool("talentbridge_search_jobs", "Tìm tin ACTIVE theo từ khóa hoặc thành phố.", Map.of(
                        "keyword", stringSchema("Từ khóa"),
                        "city", stringSchema("Thành phố"),
                        "limit", Map.of("type", "integer", "description", "Số lượng kết quả (1-20)")), List.of()),
                tool("talentbridge_get_candidate_profile", "Xem hồ sơ tối thiểu của ứng viên đã nộp vào tin thuộc công ty của bạn.", Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "candidateId", integerSchema("Mã ứng viên")), List.of("jobId", "candidateId")),
                tool("talentbridge_evaluate_cv_fit", "Đánh giá tư vấn ứng viên đã nộp vào tin tuyển dụng của bạn.", Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "candidateId", integerSchema("Mã ứng viên")), List.of("jobId", "candidateId")),
                tool("talentbridge_update_application_stage", "Chuyển ứng viên sang vòng tuyển dụng hợp lệ và lưu lịch sử.", Map.of(
                        "jobId", integerSchema("Mã tin tuyển dụng"),
                        "applicationId", integerSchema("Mã đơn ứng tuyển"),
                        "stage", stringSchema("Giai đoạn mới"),
                        "note", stringSchema("Ghi chú tùy chọn")), List.of("jobId", "applicationId", "stage"))
        );
    }

    private Map<String, Object> tool(String name, String description,
                                     Map<String, Object> properties, List<String> required) {
        return Map.of("name", name, "description", description, "inputSchema",
                Map.of("type", "object", "properties", properties, "required", required));
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> integerSchema(String description) {
        return Map.of("type", "integer", "description", description);
    }

    private String requiredText(JsonNode source, String field) {
        String value = optionalText(source, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private String optionalText(JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (value.isMissingNode() || value.isNull()) return "";
        if (!value.isTextual()) throw new IllegalArgumentException(field + " must be a string");
        return value.asText().trim();
    }

    private Long requiredLong(JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() <= 0) {
            throw new IllegalArgumentException(field + " must be a positive integer");
        }
        return value.longValue();
    }

    private int optionalInteger(JsonNode source, String field, int defaultValue) {
        JsonNode value = source.path(field);
        if (value.isMissingNode() || value.isNull()) return defaultValue;
        if (!value.isIntegralNumber() || !value.canConvertToInt()) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        return value.intValue();
    }

    private Object jsonRpcId(JsonNode request) {
        JsonNode id = request == null ? null : request.get("id");
        return id == null || id.isNull() ? null : objectMapper.convertValue(id, Object.class);
    }

    private Map<String, Object> contentResponse(String text) {
        return Map.of("content", List.of(Map.of("type", "text", "text", text)));
    }

    private Map<String, Object> successResponse(Object id, Object result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    private Map<String, Object> errorResponse(Object id, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }
}
