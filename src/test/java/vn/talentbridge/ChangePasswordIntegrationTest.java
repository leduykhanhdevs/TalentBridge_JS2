package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.in.web.dto.request.ChangePasswordRequest;
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RoleJpaRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private vn.talentbridge.core.application.port.in.LoginUseCase loginUseCase;

    private UserJpaEntity testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(new RoleJpaEntity(null, "ROLE_CANDIDATE", "Candidate Role")));

        testUser = new UserJpaEntity();
        testUser.setEmail("changepass_user@talentbridge.vn");
        testUser.setPasswordHash(passwordEncoder.encode("CurrentPassword123!"));
        testUser.setFullName("Nguyễn Đổi Mật Khẩu");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(candidateRole));
        testUser = userRepository.save(testUser);

        userToken = loginUseCase.login(
                new vn.talentbridge.core.application.dto.LoginCommand("changepass_user@talentbridge.vn", "CurrentPassword123!")
        ).accessToken();
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công và có thể đăng nhập bằng mật khẩu mới")
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "CurrentPassword123!",
                "BrandNewPassword456!",
                "BrandNewPassword456!"
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));

        // Xác minh có thể đăng nhập bằng mật khẩu mới
        LoginRequest loginRequest = new LoginRequest("changepass_user@talentbridge.vn", "BrandNewPassword456!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("Thất bại khi mật khẩu hiện tại không chính xác")
    void changePassword_WrongCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPassword999!",
                "BrandNewPassword456!",
                "BrandNewPassword456!"
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40001));
    }

    @Test
    @DisplayName("Thất bại khi xác nhận mật khẩu không khớp")
    void changePassword_MismatchedConfirmPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "CurrentPassword123!",
                "BrandNewPassword456!",
                "DifferentConfirmPassword789!"
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40001));
    }

    @Test
    @DisplayName("Thất bại khi mật khẩu mới trùng mật khẩu hiện tại")
    void changePassword_SameAsOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "CurrentPassword123!",
                "CurrentPassword123!",
                "CurrentPassword123!"
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40001));
    }

    @Test
    @DisplayName("Thất bại khi chưa đăng nhập / thiếu token")
    void changePassword_Unauthorized() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "CurrentPassword123!",
                "BrandNewPassword456!",
                "BrandNewPassword456!"
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
