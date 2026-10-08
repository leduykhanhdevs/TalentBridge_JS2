package vn.talentbridge.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateSkillJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.core.domain.vo.ApplicationPipelineStage;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.*;

/**
 * TalentBridge Model Context Protocol (MCP) Server.
 * Triển khai giao thức chuẩn mở Model Context Protocol (Anthropic MCP Spec / JSON-RPC 2.0).
 * Cho phép các LLM Clients (Claude Desktop, Cursor, Antigravity, OpenClaw, Copilot)
 * kết nối trực tiếp vào TalentBridge để gọi công cụ và truy vấn cơ sở dữ liệu thời gian thực.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mcp")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TalentBridgeMcpController {

    private final JobJpaRepository jobRepository;
    private final CandidateJpaRepository candidateRepository;
    private final CandidateSkillJpaRepository candidateSkillRepository;
    private final ApplicationJpaRepository applicationRepository;
    private final TriVectorRagMatchingService matchingService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMcpServerInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "TalentBridge Model Context Protocol (MCP) Server",
                "version", "1.0.0",
                "protocolVersion", "2024-11-05",
                "description", "Cung cấp giao thức hành động và truy vấn dữ liệu tuyển dụng ATS thời gian thực cho Trí tuệ Nhân tạo.",
                "endpoints", Map.of(
                        "jsonRpc", "/api/v1/mcp",
                        "supportedMethods", List.of("tools/list", "tools/call", "ping")
                ),
                "toolsAvailable", List.of(
                        "talentbridge_search_jobs",
                        "talentbridge_get_candidate_profile",
                        "talentbridge_evaluate_cv_fit",
                        "talentbridge_update_application_stage"
                )
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleJsonRpc(@RequestBody Map<String, Object> request) {
        String jsonrpc = (String) request.getOrDefault("jsonrpc", "2.0");
        Object id = request.get("id");
        String method = (String) request.get("method");

        if (method == null) {
            return ResponseEntity.badRequest().body(createErrorResponse(id, -32600, "Invalid Request: Missing method"));
        }

        try {
            switch (method) {
                case "ping":
                    return ResponseEntity.ok(createSuccessResponse(id, Map.of("status", "pong")));

                case "tools/list":
                    return ResponseEntity.ok(createSuccessResponse(id, Map.of("tools", getAvailableTools())));

                case "tools/call":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> params = (Map<String, Object>) request.get("params");
                    if (params == null) {
                        return ResponseEntity.badRequest().body(createErrorResponse(id, -32602, "Invalid params: Missing params"));
                    }
                    String toolName = (String) params.get("name");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Collections.emptyMap());

                    Map<String, Object> toolResult = executeTool(toolName, arguments);
                    return ResponseEntity.ok(createSuccessResponse(id, toolResult));

                default:
                    return ResponseEntity.ok(createErrorResponse(id, -32601, "Method not found: " + method));
            }
        } catch (Exception e) {
            log.error("[MCP Server] Lỗi thực thi phương thức JSON-RPC: {}", method, e);
            return ResponseEntity.ok(createErrorResponse(id, -32603, "Internal error: " + e.getMessage()));
        }
    }

    private List<Map<String, Object>> getAvailableTools() {
        return List.of(
                Map.of(
                        "name", "talentbridge_search_jobs",
                        "description", "Tìm kiếm các việc làm đang tuyển dụng trong hệ thống TalentBridge theo từ khóa, thành phố hoặc cấp bậc",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "keyword", Map.of("type", "string", "description", "Từ khóa tìm kiếm (VD: Java, React, Backend)"),
                                        "city", Map.of("type", "string", "description", "Thành phố làm việc (VD: Hồ Chí Minh, Hà Nội)"),
                                        "limit", Map.of("type", "integer", "description", "Số lượng kết quả tối đa cần lấy (mặc định 5)")
                                )
                        )
                ),
                Map.of(
                        "name", "talentbridge_get_candidate_profile",
                        "description", "Tra cứu hồ sơ chi tiết, kỹ năng và kinh nghiệm làm việc của ứng viên theo ID",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "candidateId", Map.of("type", "integer", "description", "ID của ứng viên")
                                ),
                                "required", List.of("candidateId")
                        )
                ),
                Map.of(
                        "name", "talentbridge_evaluate_cv_fit",
                        "description", "Đánh giá độ phù hợp của ứng viên với công việc theo thuật toán Tri-Vector RAG từ bài báo khoa học SoftwareX 2025",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "jobId", Map.of("type", "integer", "description", "ID của công việc"),
                                        "candidateId", Map.of("type", "integer", "description", "ID của ứng viên"),
                                        "cvText", Map.of("type", "string", "description", "Nội dung văn bản CV thô (tùy chọn)")
                                ),
                                "required", List.of("jobId")
                        )
                ),
                Map.of(
                        "name", "talentbridge_update_application_stage",
                        "description", "Chuyển giai đoạn tuyển dụng của ứng viên trong quy trình ATS (APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED)",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "applicationId", Map.of("type", "integer", "description", "Mã đơn ứng tuyển"),
                                        "stage", Map.of("type", "string", "description", "Trạng thái mới (APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED)")
                                ),
                                "required", List.of("applicationId", "stage")
                        )
                )
        );
    }

    private Map<String, Object> executeTool(String toolName, Map<String, Object> args) {
        log.info("[MCP Tool Call] Thực thi công cụ: {} với tham số: {}", toolName, args);

        switch (toolName) {
            case "talentbridge_search_jobs": {
                String keyword = (String) args.getOrDefault("keyword", "");
                int limit = args.get("limit") instanceof Number ? ((Number) args.get("limit")).intValue() : 5;
                var jobs = jobRepository.findAll();
                List<Map<String, Object>> matched = new ArrayList<>();
                for (JobJpaEntity j : jobs) {
                    if (j.getStatus() == JobStatus.ACTIVE) {
                        if (keyword.isBlank() || j.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                            matched.add(Map.of(
                                    "id", j.getId(),
                                    "title", j.getTitle(),
                                    "company", j.getCompany() != null ? j.getCompany().getName() : "N/A",
                                    "location", j.getLocation() != null ? j.getLocation() : "Toàn quốc",
                                    "experience", j.getExperienceLevel() != null ? j.getExperienceLevel() : "Không yêu cầu"
                            ));
                            if (matched.size() >= limit) break;
                        }
                    }
                }
                return createMcpContentResponse("Tìm thấy " + matched.size() + " công việc phù hợp:\n" + toJson(matched));
            }

            case "talentbridge_get_candidate_profile": {
                Long candidateId = ((Number) args.get("candidateId")).longValue();
                CandidateJpaEntity c = candidateRepository.findById(candidateId).orElse(null);
                if (c == null) {
                    return createMcpContentResponse("Không tìm thấy ứng viên với ID: " + candidateId);
                }
                var skills = candidateSkillRepository.findByCandidateIdOrderByIdAsc(candidateId);
                List<String> skillNames = skills.stream()
                        .map(s -> s.getSkill() != null ? s.getSkill().getName() : "")
                        .filter(s -> !s.isBlank())
                        .toList();

                Map<String, Object> profile = Map.of(
                        "id", c.getId(),
                        "name", c.getUser() != null ? c.getUser().getFullName() : "N/A",
                        "title", c.getTitle() != null ? c.getTitle() : "N/A",
                        "experienceYears", c.getExperienceYears(),
                        "summary", c.getSummary() != null ? c.getSummary() : "",
                        "skills", skillNames
                );
                return createMcpContentResponse("Thông tin ứng viên:\n" + toJson(profile));
            }

            case "talentbridge_evaluate_cv_fit": {
                Long jobId = ((Number) args.get("jobId")).longValue();
                Long candidateId = args.get("candidateId") != null ? ((Number) args.get("candidateId")).longValue() : null;
                String cvText = (String) args.get("cvText");

                var result = matchingService.matchCandidateToJob(jobId, candidateId, cvText);
                return createMcpContentResponse("Kết quả đánh giá Tri-Vector RAG:\n" + toJson(result));
            }

            case "talentbridge_update_application_stage": {
                Long appId = ((Number) args.get("applicationId")).longValue();
                String stageStr = (String) args.get("stage");

                ApplicationJpaEntity app = applicationRepository.findById(appId).orElse(null);
                if (app == null) {
                    return createMcpContentResponse("Không tìm thấy đơn ứng tuyển ID: " + appId);
                }

                try {
                    ApplicationPipelineStage stage = ApplicationPipelineStage.valueOf(stageStr.toUpperCase());
                    app.setCurrentStage(stage.name());
                    applicationRepository.save(app);
                    return createMcpContentResponse("Cập nhật thành công đơn ứng tuyển #" + appId + " sang giai đoạn: " + stage);
                } catch (IllegalArgumentException e) {
                    return createMcpContentResponse("Trạng thái không hợp lệ: " + stageStr + ". Các trạng thái hợp lệ: APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED");
                }
            }

            default:
                return createMcpContentResponse("Không tìm thấy công cụ mang tên: " + toolName);
        }
    }

    private Map<String, Object> createMcpContentResponse(String text) {
        return Map.of(
                "content", List.of(
                        Map.of(
                                "type", "text",
                                "text", text
                        )
                )
        );
    }

    private Map<String, Object> createSuccessResponse(Object id, Object result) {
        return Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : 1,
                "result", result
        );
    }

    private Map<String, Object> createErrorResponse(Object id, int code, String message) {
        return Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : 1,
                "error", Map.of(
                        "code", code,
                        "message", message
                )
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
