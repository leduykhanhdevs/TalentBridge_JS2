package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TalentBridgeMcpProtocolIntegrationTest {
    private static final String ENDPOINT = "/api/v1/mcp";

    @Autowired private UserJpaRepository userRepository;
    @Autowired private RoleJpaRepository roleRepository;
    @Autowired private RecruiterJpaRepository recruiterRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private LoginUseCase loginUseCase;
    @Autowired private AuthSessionRepositoryPort authSessionRepository;
    @Autowired private TokenProviderPort tokenProvider;
    @LocalServerPort private int port;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String recruiterToken;
    private String candidateToken;

    @BeforeEach
    void setUp() {
        String email = "mcp-" + UUID.randomUUID() + "@talentbridge.vn";

        RoleJpaEntity recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder()
                        .name("ROLE_RECRUITER").description("Recruiter role").build()));
        UserJpaEntity user = userRepository.save(UserJpaEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("ProtocolTest123!"))
                .fullName("MCP Protocol Test")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(List.of(recruiterRole)))
                .build());
        recruiterRepository.save(RecruiterJpaEntity.builder().user(user).position("HR").build());
        recruiterToken = loginUseCase.login(new LoginCommand(email, "ProtocolTest123!")).accessToken();

        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder()
                        .name("ROLE_CANDIDATE").description("Candidate role").build()));
        UserJpaEntity candidate = userRepository.save(UserJpaEntity.builder()
                .email("candidate-" + UUID.randomUUID() + "@talentbridge.vn")
                .passwordHash(passwordEncoder.encode("ProtocolTest123!"))
                .fullName("MCP Candidate Test")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(List.of(candidateRole)))
                .build());
        candidateToken = loginUseCase.login(
                new LoginCommand(candidate.getEmail(), "ProtocolTest123!")).accessToken();
    }

    @Test
    void recruiterCompletesInitializeListAndGroundedToolCallOverStreamableHttp() {
        HttpResponse<String> initialized = post("""
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
                  "protocolVersion":"2025-11-25","capabilities":{},
                  "clientInfo":{"name":"talentbridge-integration-test","version":"1.0.0"}}}
                """, recruiterToken, null);

        assertThat(initialized.statusCode()).isBetween(200, 299);
        String sessionId = initialized.headers().firstValue("Mcp-Session-Id").orElse(null);
        assertThat(sessionId).isNotBlank();

        HttpResponse<String> notification = post(
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", recruiterToken, sessionId);
        assertThat(notification.statusCode()).isEqualTo(202);

        HttpResponse<String> listed = post(
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}", recruiterToken, sessionId);
        assertThat(listed.statusCode()).isBetween(200, 299);
        assertThat(listed.body()).contains("talentbridge_search_help", "talentbridge_search_jobs", "talentbridge_update_application_stage", "confirm");

        HttpResponse<String> called = post("""
                {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{
                  "name":"talentbridge_search_help",
                  "arguments":{"query":"Làm sao để ứng tuyển việc làm trên TalentBridge?","limit":3}}}
                """, recruiterToken, sessionId);
        assertThat(called.statusCode()).isBetween(200, 299);
        assertThat(called.body()).contains("Việc làm", "Tìm việc và ứng tuyển");
    }

    @Test
    void mcpWriteToolRequiresExplicitConfirmationBeforeCallingBusinessUseCase() {
        HttpResponse<String> initialized = post("""
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
                  "protocolVersion":"2025-11-25","capabilities":{},
                  "clientInfo":{"name":"talentbridge-confirmation-test","version":"1.0.0"}}}
                """, recruiterToken, null);
        assertThat(initialized.statusCode()).isBetween(200, 299);
        String sessionId = initialized.headers().firstValue("Mcp-Session-Id").orElseThrow();
        post("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", recruiterToken, sessionId);

        HttpResponse<String> response = post("""
                {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{
                  "name":"talentbridge_update_application_stage",
                  "arguments":{"jobId":1,"applicationId":1,"stage":"SCREENING","confirm":false}}}
                """, recruiterToken, sessionId);

        assertThat(response.statusCode()).isBetween(200, 299);
        assertThat(response.body()).contains("confirm=true");
    }

    @Test
    void anonymousRequestCannotAccessMcpServlet() {
        HttpResponse<String> response = post(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}", null, null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void candidateRoleCannotInitializeRecruiterMcpServer() {
        assertThat(tokenProvider.validateToken(candidateToken)).isTrue();
        assertThat(tokenProvider.getTokenTypeFromToken(candidateToken)).isEqualTo("access");
        assertThat(tokenProvider.getRoleFromToken(candidateToken)).isEqualTo("ROLE_CANDIDATE");
        var candidateSession = authSessionRepository.findActiveBySessionId(
                tokenProvider.getSessionIdFromToken(candidateToken), LocalDateTime.now());
        assertThat(candidateSession).isPresent();
        assertThat(candidateSession.orElseThrow().getUserId())
                .isEqualTo(tokenProvider.getUserIdFromToken(candidateToken));

        HttpResponse<String> response = post(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}",
                candidateToken, null);

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("40301");
    }

    private HttpResponse<String> post(String body, String token, String sessionId) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json, text/event-stream");
            if (token != null) request.header("Authorization", "Bearer " + token);
            if (sessionId != null) request.header("Mcp-Session-Id", sessionId);
            return httpClient.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            throw new AssertionError("MCP HTTP request failed", exception);
        }
    }
}
