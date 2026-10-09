package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.in.security.UserPrincipal;
import vn.talentbridge.core.application.dto.McpJobSummary;
import vn.talentbridge.core.application.port.in.TalentBridgeMcpUseCase;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TalentBridgeMcpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TalentBridgeMcpUseCase mcpUseCase;

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("MCP Server: GET /api/v1/mcp trả về thông tin máy chủ và danh mục công cụ")
    void shouldReturnMcpServerInfo() throws Exception {
        mockMvc.perform(get("/api/v1/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", containsString("TalentBridge")))
                .andExpect(jsonPath("$.jsonRpcVersion", is("2.0")))
                .andExpect(jsonPath("$.toolsAvailable", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("MCP JSON-RPC 2.0: tools/list trả về danh sách các công cụ tuyển dụng ATS")
    void shouldReturnToolsListViaJsonRpc() throws Exception {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/list"
        );

        mockMvc.perform(post("/api/v1/mcp")
                        .with(SecurityMockMvcRequestPostProcessors.user(new UserPrincipal(1L, "recruiter@talentbridge.vn", "RECRUITER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc", is("2.0")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.result.tools", hasSize(4)))
                .andExpect(jsonPath("$.result.tools[*].name", hasItem("talentbridge_search_jobs")))
                .andExpect(jsonPath("$.result.tools[*].name", hasItem("talentbridge_evaluate_cv_fit")));
    }

    @Test
    @DisplayName("MCP JSON-RPC 2.0: tools/call thực thi tìm kiếm việc làm talentbridge_search_jobs")
    void shouldExecuteToolSearchJobs() throws Exception {
        when(mcpUseCase.searchJobs(1L, "Java", "", 3))
                .thenReturn(java.util.List.of(new McpJobSummary(1L, "Java Developer", "TalentBridge", "Hà Nội", "Junior")));
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", 2,
                "method", "tools/call",
                "params", Map.of(
                        "name", "talentbridge_search_jobs",
                        "arguments", Map.of(
                            "keyword", "Java",
                                "limit", 3
                        )
                )
        );

        mockMvc.perform(post("/api/v1/mcp")
                        .with(SecurityMockMvcRequestPostProcessors.user(new UserPrincipal(1L, "recruiter@talentbridge.vn", "RECRUITER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc", is("2.0")))
                .andExpect(jsonPath("$.result.content[0].type", is("text")))
                .andExpect(jsonPath("$.result.content[0].text", containsString("title")));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("MCP: từ chối thiếu jobId thay vì tra cứu hồ sơ ứng viên toàn cục")
    void shouldRequireJobScopeForCandidateProfile() throws Exception {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", 3,
                "method", "tools/call",
                "params", Map.of(
                        "name", "talentbridge_get_candidate_profile",
                        "arguments", Map.of("candidateId", 1)
                )
        );

        mockMvc.perform(post("/api/v1/mcp")
                        .with(SecurityMockMvcRequestPostProcessors.user(new UserPrincipal(1L, "recruiter@talentbridge.vn", "RECRUITER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code", is(-32602)))
                .andExpect(jsonPath("$.error.message", containsString("jobId")));
    }

    @Test
    @DisplayName("MCP Server: từ chối request ẩn danh vì có công cụ đọc và cập nhật dữ liệu ATS")
    void shouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/mcp"))
                .andExpect(status().isUnauthorized());
    }
}
