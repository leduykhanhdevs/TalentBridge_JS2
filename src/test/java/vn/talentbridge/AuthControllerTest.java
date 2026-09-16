package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;
import vn.talentbridge.adapter.in.web.dto.request.RegisterRequest;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import vn.talentbridge.adapter.in.web.dto.request.RefreshTokenRequest;
import org.springframework.test.util.ReflectionTestUtils;
import vn.talentbridge.adapter.out.security.JwtTokenProviderAdapter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CandidateJpaRepository candidateJpaRepository;

    @Autowired
    private RecruiterJpaRepository recruiterJpaRepository;

    @Autowired
    private JobJpaRepository jobJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private JwtTokenProviderAdapter jwtTokenProviderAdapter;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        candidateJpaRepository.deleteAll();
        recruiterJpaRepository.deleteAll();
        jobJpaRepository.deleteAll();
        companyJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
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

    @Test
    @DisplayName("POST /api/v1/auth/logout - Không có token trả về HTTP 401")
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40101));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Thu hồi Access Token và Refresh Token")
    void testLogoutRevokesAccessAndRefreshToken() throws Exception {
        TokenPair tokens = registerAndGetTokens(
                "logout@test.com"
        );

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + tokens.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Đăng xuất thành công"))
                .andExpect(jsonPath("$.data").doesNotExist());

        // Access token của phiên vừa logout không còn sử dụng được.
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(
                                "Authorization",
                                "Bearer " + tokens.accessToken()
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40101));

        RefreshTokenRequest refreshRequest =
                RefreshTokenRequest.builder()
                        .refreshToken(tokens.refreshToken())
                        .build();

        // Refresh token cùng phiên cũng không còn sử dụng được.
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                refreshRequest
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Đăng xuất một phiên không ảnh hưởng phiên khác")
    void testLogoutDoesNotRevokeOtherSession() throws Exception {
        String email = "multiple-sessions@test.com";

        TokenPair firstSession = registerAndGetTokens(email);
        TokenPair secondSession = loginAndGetTokens(email);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + firstSession.accessToken()
                        ))
                .andExpect(status().isOk());

        // Phiên thứ hai vẫn gọi được API được bảo vệ.
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(
                                "Authorization",
                                "Bearer " + secondSession.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // Refresh token của phiên thứ hai vẫn hoạt động.
        RefreshTokenRequest refreshRequest =
                RefreshTokenRequest.builder()
                        .refreshToken(secondSession.refreshToken())
                        .build();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                refreshRequest
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Phiên đã logout không thể logout lần nữa")
    void testRepeatedLogoutReturnsUnauthorized() throws Exception {
        TokenPair tokens = registerAndGetTokens(
                "repeated-logout@test.com"
        );

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + tokens.accessToken()
                        ))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + tokens.accessToken()
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40101));
    }

    @Test
    @DisplayName("Access Token hết hạn gọi API bảo vệ trả về HTTP 401")
    void testExpiredAccessTokenReturnsUnauthorized() throws Exception {
        String email = "expired-access@test.com";

        TokenPair tokens = registerAndGetTokens(email);

        Long userId = jwtTokenProviderAdapter.getUserIdFromToken(
                tokens.accessToken()
        );

        String sessionId =
                jwtTokenProviderAdapter.getSessionIdFromToken(
                        tokens.accessToken()
                );

        long originalExpirationMs =
                jwtTokenProviderAdapter.getExpirationMs();

        String expiredAccessToken;

        try {
            ReflectionTestUtils.setField(
                    jwtTokenProviderAdapter,
                    "jwtExpirationMs",
                    -1_000L
            );

            expiredAccessToken =
                    jwtTokenProviderAdapter.generateAccessToken(
                            userId,
                            email,
                            "ROLE_CANDIDATE",
                            sessionId
                    );
        } finally {
            ReflectionTestUtils.setField(
                    jwtTokenProviderAdapter,
                    "jwtExpirationMs",
                    originalExpirationMs
            );
        }

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(
                                "Authorization",
                                "Bearer " + expiredAccessToken
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(40101));
    }

    private TokenPair registerAndGetTokens(String email)
            throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .password("password123")
                .fullName("Logout Test")
                .role("ROLE_CANDIDATE")
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return readTokens(result);
    }

    private TokenPair loginAndGetTokens(String email)
            throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isOk())
                .andReturn();

        return readTokens(result);
    }

    private TokenPair readTokens(MvcResult result) throws Exception {
        String responseBody =
                result.getResponse().getContentAsString();

        String accessToken =
                com.jayway.jsonpath.JsonPath.read(
                        responseBody,
                        "$.data.accessToken"
                );

        String refreshToken =
                com.jayway.jsonpath.JsonPath.read(
                        responseBody,
                        "$.data.refreshToken"
                );

        return new TokenPair(accessToken, refreshToken);
    }

    private record TokenPair(
            String accessToken,
            String refreshToken
    ) {
    }

}