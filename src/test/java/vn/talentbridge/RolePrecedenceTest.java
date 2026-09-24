package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.LoginUseCaseImpl;
import vn.talentbridge.core.application.usecase.RefreshTokenUseCaseImpl;
import vn.talentbridge.core.domain.model.AuthSession;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePrecedenceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private AuthSessionRepositoryPort authSessionRepository;

    private LoginUseCaseImpl loginUseCase;
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCaseImpl(
                userRepository,
                passwordEncoder,
                tokenProvider,
                authSessionRepository,
                null,
                900000L,
                604800000L
        );

        refreshTokenUseCase = new RefreshTokenUseCaseImpl(
                userRepository,
                tokenProvider,
                authSessionRepository,
                3600000L
        );
    }

    private User buildUserWithRoles(RoleName... roleNames) {
        User user = new User();
        user.setId(100L);
        user.setEmail("user@talentbridge.vn");
        user.setPasswordHash("hashed_pwd");
        user.activate();

        Set<Role> roles = new LinkedHashSet<>();
        long id = 1L;
        for (RoleName rn : roleNames) {
            roles.add(new Role(id++, rn, rn.name()));
        }
        user.setRoles(roles);
        return user;
    }

    @Test
    @DisplayName("Login: User with CANDIDATE, RECRUITER, ADMIN should resolve primary role as ADMIN")
    void login_withMultipleRoles_prefersAdmin() {
        // CANDIDATE is inserted first into LinkedHashSet to expose findFirst() bug
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE, RoleName.ROLE_RECRUITER, RoleName.ROLE_ADMIN);

        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_pwd")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");
        when(tokenProvider.hashRefreshToken(anyString())).thenReturn("hashed-refresh");

        loginUseCase.login(new LoginCommand("user@talentbridge.vn", "Password123!"));

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_ADMIN", roleCaptor.getValue());
    }

    @Test
    @DisplayName("Login: User with CANDIDATE and RECRUITER should resolve primary role as RECRUITER")
    void login_withRecruiterAndCandidate_prefersRecruiter() {
        // CANDIDATE is inserted first into LinkedHashSet
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE, RoleName.ROLE_RECRUITER);

        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_pwd")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");
        when(tokenProvider.hashRefreshToken(anyString())).thenReturn("hashed-refresh");

        loginUseCase.login(new LoginCommand("user@talentbridge.vn", "Password123!"));

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_RECRUITER", roleCaptor.getValue());
    }

    @Test
    @DisplayName("Login: User with only CANDIDATE should resolve primary role as CANDIDATE")
    void login_withOnlyCandidate_resolvesCandidate() {
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE);

        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_pwd")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");
        when(tokenProvider.hashRefreshToken(anyString())).thenReturn("hashed-refresh");

        loginUseCase.login(new LoginCommand("user@talentbridge.vn", "Password123!"));

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_CANDIDATE", roleCaptor.getValue());
    }

    @Test
    @DisplayName("RefreshToken: User with CANDIDATE, RECRUITER, ADMIN should resolve primary role as ADMIN")
    void refreshToken_withMultipleRoles_prefersAdmin() {
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE, RoleName.ROLE_RECRUITER, RoleName.ROLE_ADMIN);

        LocalDateTime now = LocalDateTime.now();
        AuthSession session = new AuthSession(1L, "sess-1", 100L, "hash", now, now.plusHours(1), null);

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken("valid-token")).thenReturn("refresh");
        when(tokenProvider.getSessionIdFromToken("valid-token")).thenReturn("sess-1");
        when(authSessionRepository.findActiveBySessionId(eq("sess-1"), any())).thenReturn(Optional.of(session));
        when(tokenProvider.matchesRefreshTokenHash("valid-token", "hash")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid-token")).thenReturn(100L);
        when(tokenProvider.getEmailFromToken("valid-token")).thenReturn("user@talentbridge.vn");
        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(authSessionRepository.rotateRefreshTokenIfActive(any(), any(), any(), any(), any())).thenReturn(true);

        refreshTokenUseCase.refreshToken("valid-token");

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_ADMIN", roleCaptor.getValue());
    }

    @Test
    @DisplayName("RefreshToken: User with CANDIDATE and RECRUITER should resolve primary role as RECRUITER")
    void refreshToken_withRecruiterAndCandidate_prefersRecruiter() {
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE, RoleName.ROLE_RECRUITER);

        LocalDateTime now = LocalDateTime.now();
        AuthSession session = new AuthSession(1L, "sess-2", 100L, "hash", now, now.plusHours(1), null);

        when(tokenProvider.validateToken("valid-token-2")).thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken("valid-token-2")).thenReturn("refresh");
        when(tokenProvider.getSessionIdFromToken("valid-token-2")).thenReturn("sess-2");
        when(authSessionRepository.findActiveBySessionId(eq("sess-2"), any())).thenReturn(Optional.of(session));
        when(tokenProvider.matchesRefreshTokenHash("valid-token-2", "hash")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid-token-2")).thenReturn(100L);
        when(tokenProvider.getEmailFromToken("valid-token-2")).thenReturn("user@talentbridge.vn");
        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(authSessionRepository.rotateRefreshTokenIfActive(any(), any(), any(), any(), any())).thenReturn(true);

        refreshTokenUseCase.refreshToken("valid-token-2");

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_RECRUITER", roleCaptor.getValue());
    }

    @Test
    @DisplayName("RefreshToken: User with only CANDIDATE should resolve primary role as CANDIDATE")
    void refreshToken_withOnlyCandidate_resolvesCandidate() {
        User user = buildUserWithRoles(RoleName.ROLE_CANDIDATE);

        LocalDateTime now = LocalDateTime.now();
        AuthSession session = new AuthSession(1L, "sess-3", 100L, "hash", now, now.plusHours(1), null);

        when(tokenProvider.validateToken("valid-token-3")).thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken("valid-token-3")).thenReturn("refresh");
        when(tokenProvider.getSessionIdFromToken("valid-token-3")).thenReturn("sess-3");
        when(authSessionRepository.findActiveBySessionId(eq("sess-3"), any())).thenReturn(Optional.of(session));
        when(tokenProvider.matchesRefreshTokenHash("valid-token-3", "hash")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid-token-3")).thenReturn(100L);
        when(tokenProvider.getEmailFromToken("valid-token-3")).thenReturn("user@talentbridge.vn");
        when(userRepository.findByEmail("user@talentbridge.vn")).thenReturn(Optional.of(user));
        when(authSessionRepository.rotateRefreshTokenIfActive(any(), any(), any(), any(), any())).thenReturn(true);

        refreshTokenUseCase.refreshToken("valid-token-3");

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenProvider).generateAccessToken(eq(100L), eq("user@talentbridge.vn"), roleCaptor.capture(), any(), any());

        assertEquals("ROLE_CANDIDATE", roleCaptor.getValue());
    }
}
