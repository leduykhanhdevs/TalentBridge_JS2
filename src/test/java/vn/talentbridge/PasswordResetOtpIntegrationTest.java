package vn.talentbridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.out.persistence.repository.PasswordResetTokenJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "talentbridge.password-reset.mail-enabled=true",
        "talentbridge.rate-limit.auth-limit=10"
})
class PasswordResetOtpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordResetTokenJpaRepository tokenRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @MockBean
    private PasswordResetEmailPort emailPort;

    @Test
    void forgotPasswordResponseDoesNotRevealWhetherEmailDeliveryFailed() throws Exception {
        String registeredEmail = "enumeration-" + UUID.randomUUID() + "@example.test";
        String unknownEmail = "missing-" + UUID.randomUUID() + "@example.test";
        String password = "OriginalPass123";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.41")
                        .content(objectMapper.writeValueAsString(new RegisterBody(
                                registeredEmail,
                                password,
                                "Enumeration Test",
                                "ROLE_CANDIDATE"
                        ))))
                .andExpect(status().isCreated());

        doThrow(new PasswordResetMailUnavailableException())
                .when(emailPort)
                .sendPasswordResetOtp(eq(registeredEmail), anyString(), any());

        String failedDeliveryResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.41")
                        .content(objectMapper.writeValueAsString(new EmailBody(registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Nếu email tồn tại")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(emailPort, timeout(5_000))
                .sendPasswordResetOtp(eq(registeredEmail), anyString(), any());

        String unknownEmailResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.41")
                        .content(objectMapper.writeValueAsString(new EmailBody(unknownEmail))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(failedDeliveryResponse).path("message").asText())
                .isEqualTo(objectMapper.readTree(unknownEmailResponse).path("message").asText());
    }

    @Test
    void emailOtpMustBeVerifiedBeforePasswordCanBeChanged() throws Exception {
        String email = "otp-" + UUID.randomUUID() + "@example.test";
        String firstPassword = "OriginalPass123";
        String newPassword = "UpdatedPass123";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new RegisterBody(
                                email,
                                firstPassword,
                                "OTP Integration",
                                "ROLE_CANDIDATE"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new EmailBody(email))))
                .andExpect(status().isOk());

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort, timeout(5_000))
                .sendPasswordResetOtp(eq(email), otpCaptor.capture(), any());
        String otp = otpCaptor.getValue();
        assertThat(otp).matches("[0-9]{6}");
        String wrongOtp = otp.equals("000000") ? "000001" : "000000";

        mockMvc.perform(post("/api/v1/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new VerifyOtpBody(email, wrongOtp))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("nhập lại")));

        var user = userRepository.findByEmail(email).orElseThrow();
        var otpRecord = tokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(otpRecord.getFailedAttempts()).isEqualTo(1);
        assertThat(otpRecord.getPurpose().name()).isEqualTo("OTP");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new ResetBody(otp, newPassword))))
                .andExpect(status().isBadRequest());

        String verifyResponse = mockMvc.perform(post("/api/v1/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new VerifyOtpBody(email, otp))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode response = objectMapper.readTree(verifyResponse);
        String resetGrant = response.path("data").path("resetToken").asText();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new ResetBody(resetGrant, newPassword))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new LoginBody(email, newPassword))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "192.0.2.40")
                        .content(objectMapper.writeValueAsString(new LoginBody(email, firstPassword))))
                .andExpect(status().isUnauthorized());
    }

    private record RegisterBody(String email, String password, String fullName, String role) { }

    private record EmailBody(String email) { }

    private record VerifyOtpBody(String email, String otp) { }

    private record ResetBody(String token, String newPassword) { }

    private record LoginBody(String email, String password) { }
}
