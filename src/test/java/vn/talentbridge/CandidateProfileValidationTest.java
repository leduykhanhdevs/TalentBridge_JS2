package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.talentbridge.adapter.in.web.dto.request.UpdateCandidateProfileRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CandidateProfileValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "candidate@talentbridge.vn", roles = {"CANDIDATE"})
    @DisplayName("Should reject candidate profile update when phone or URL format is invalid")
    void testInvalidPhoneAndUrlValidation() throws Exception {
        UpdateCandidateProfileRequest request = UpdateCandidateProfileRequest.builder()
                .fullName("Test Candidate")
                .phone("not-a-phone-number")
                .avatarUrl("ftp://invalid-avatar")
                .personalWebsite("invalid-url")
                .linkedinUrl("invalid-linkedin")
                .githubUrl("invalid-github")
                .gender("INVALID_GENDER")
                .build();

        mockMvc.perform(put("/api/v1/candidates/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "candidate@talentbridge.vn", roles = {"CANDIDATE"})
    @DisplayName("Should reject invalid URLs and gender even when phone is valid")
    void testInvalidUrlAndGenderValidation() throws Exception {
        UpdateCandidateProfileRequest request = UpdateCandidateProfileRequest.builder()
                .fullName("Valid Name")
                .phone("0912345678")
                .avatarUrl("ftp://invalid-avatar")
                .personalWebsite("not-a-http-url")
                .linkedinUrl("javascript:void(0)")
                .githubUrl("ftp://github")
                .gender("UNKNOWN")
                .build();

        mockMvc.perform(put("/api/v1/candidates/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
