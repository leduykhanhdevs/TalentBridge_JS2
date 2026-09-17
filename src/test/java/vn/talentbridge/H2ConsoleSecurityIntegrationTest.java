package vn.talentbridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class H2ConsoleSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("H2 console must be protected and NOT permitted in non-local profiles")
    void h2ConsoleShouldBeProtectedInNonLocalProfiles() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized());
    }
}
