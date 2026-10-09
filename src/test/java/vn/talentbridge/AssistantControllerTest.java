package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.core.application.dto.AssistantChatResult;
import vn.talentbridge.core.application.port.in.AskTalentBridgeAssistantUseCase;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssistantControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AskTalentBridgeAssistantUseCase assistantUseCase;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void authenticatedCandidateCanAskWithinProductScope() throws Exception {
        when(assistantUseCase.ask(eq("Làm sao tìm việc?"), anyList()))
                .thenReturn(new AssistantChatResult("Mở mục Việc làm.", "KNOWLEDGE_BASE", List.of("Tìm việc và ứng tuyển")));

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "message", "Làm sao tìm việc?",
                                "history", List.of(java.util.Map.of("role", "USER", "content", "Xin chào"))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value("Mở mục Việc làm."))
                .andExpect(jsonPath("$.data.source").value("KNOWLEDGE_BASE"));
    }

    @Test
    void anonymousUserCannotUseAssistant() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Làm sao tìm việc?\"}"))
                .andExpect(status().isUnauthorized());
    }
}
