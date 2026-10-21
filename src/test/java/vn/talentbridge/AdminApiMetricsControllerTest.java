package vn.talentbridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    @DisplayName("Metrics endpoint từ chối người chưa đăng nhập")
    void metricsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metrics/api"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Metrics endpoint chỉ dành cho admin")
    void metricsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metrics/api"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin xem được thống kê request theo endpoint và khoảng thời gian")
    void adminCanReadEndpointMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/jobs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/metrics/api").param("hours", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bucketMinutes").value(5))
                .andExpect(jsonPath("$.data.retentionHours").value(24))
                .andExpect(jsonPath("$.data.resetsOnRestart").value(true))
                .andExpect(jsonPath("$.data.requestCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.endpoints[0].endpoint").exists())
                .andExpect(jsonPath("$.data.endpoints[0].buckets[0].startsAt").exists());
    }
}
