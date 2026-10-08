package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiMatchingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsAnonymousMatchingRequests() throws Exception {
        mockMvc.perform(post("/api/v1/ai/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":1,\"candidateId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void rejectsCandidateRoleFromRecruiterMatchingEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/ai/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":1,\"candidateId\":1}"))
                .andExpect(status().isForbidden());
    }
}
