package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.talentbridge.modules.auth.dto.request.LoginRequest;
import vn.talentbridge.modules.auth.dto.request.RegisterRequest;
import vn.talentbridge.modules.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Đăng ký thành công trả về HTTP 201 và Token")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("candidate@test.com")
                .password("password123")
                .fullName("Nguyen Van A")
                .phone("0912345678")
                .role("ROLE_CANDIDATE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("candidate@test.com"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_CANDIDATE"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Đăng ký trùng email trả về HTTP 409 Conflict")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@test.com")
                .password("password123")
                .fullName("Tran Van B")
                .role("ROLE_CANDIDATE")
                .build();

        // Register first time
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register second time with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(40901));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Dữ liệu không hợp lệ trả về HTTP 400 Bad Request")
    void testRegisterInvalidInput() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("123") // < 6 chars
                .fullName("")
                .role("ROLE_INVALID")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(40002));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Đăng nhập hợp lệ trả về HTTP 200")
    void testLoginSuccess() throws Exception {
        // Register account
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("login@test.com")
                .password("secret123")
                .fullName("Le Duy Khanh")
                .role("ROLE_RECRUITER")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginReq = LoginRequest.builder()
                .email("login@test.com")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@test.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Sai mật khẩu trả về HTTP 401 Unauthorized")
    void testLoginInvalidCredentials() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("wrongpw@test.com")
                .password("correct123")
                .fullName("User Test")
                .role("ROLE_CANDIDATE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = LoginRequest.builder()
                .email("wrongpw@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40102));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Có JWT Token trả về 200, không có token trả về 401")
    void testGetCurrentUserAuthFlow() throws Exception {
        // Without Token -> 401
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40101));

        // Register and get token
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("me@test.com")
                .password("password123")
                .fullName("Current User Test")
                .role("ROLE_CANDIDATE")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.accessToken");

        // With Bearer Token -> 200
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@test.com"))
                .andExpect(jsonPath("$.data.fullName").value("Current User Test"));
    }
}