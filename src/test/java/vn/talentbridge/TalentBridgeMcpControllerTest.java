package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
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

    @Test
    @DisplayName("MCP Server: GET /api/v1/mcp trả về thông tin máy chủ và danh mục công cụ")
    void shouldReturnMcpServerInfo() throws Exception {
        mockMvc.perform(get("/api/v1/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", containsString("Model Context Protocol")))
                .andExpect(jsonPath("$.protocolVersion", is("2024-11-05")))
                .andExpect(jsonPath("$.toolsAvailable", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("MCP JSON-RPC 2.0: tools/list trả về danh sách các công cụ tuyển dụng ATS")
    void shouldReturnToolsListViaJsonRpc() throws Exception {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/list"
        );

        mockMvc.perform(post("/api/v1/mcp")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc", is("2.0")))
                .andExpect(jsonPath("$.result.content[0].type", is("text")))
                .andExpect(jsonPath("$.result.content[0].text", containsString("Tìm thấy")));
    }
}
