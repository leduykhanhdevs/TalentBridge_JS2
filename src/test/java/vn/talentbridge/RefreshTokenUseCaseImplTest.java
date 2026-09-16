package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.RefreshTokenUseCaseImpl;
import vn.talentbridge.core.domain.exception.InvalidCredentialsException;
import vn.talentbridge.core.domain.model.AuthSession;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private AuthSessionRepositoryPort authSessionRepository;

    private RefreshTokenUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCaseImpl(
                userRepository,
                tokenProvider,
                authSessionRepository,
                3_600_000L
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("refresh-test@example.com");
        user.setFullName("Refresh Test");
        user.addRole(new Role(
                1L,
                RoleName.ROLE_CANDIDATE,
                "Candidate"
        ));

        LocalDateTime now = LocalDateTime.now();

        AuthSession session = new AuthSession(
                10L,
                "test-session",
                1L,
                "old-hash",
                now,
                now.plusHours(1),
                null
        );

        when(tokenProvider.validateToken("old-refresh"))
                .thenReturn(true);

        when(tokenProvider.getTokenTypeFromToken("old-refresh"))
                .thenReturn("refresh");

        when(tokenProvider.getSessionIdFromToken("old-refresh"))
                .thenReturn("test-session");

        when(authSessionRepository.findActiveBySessionId(
                eq("test-session"),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(session));

        when(tokenProvider.matchesRefreshTokenHash(
                "old-refresh",
                "old-hash"
        )).thenReturn(true);

        when(tokenProvider.getUserIdFromToken("old-refresh"))
                .thenReturn(1L);

        when(tokenProvider.getEmailFromToken("old-refresh"))
                .thenReturn("refresh-test@example.com");

        when(userRepository.findByEmail("refresh-test@example.com"))
                .thenReturn(Optional.of(user));

        when(tokenProvider.generateAccessToken(
                eq(1L),
                eq("refresh-test@example.com"),
                eq("ROLE_CANDIDATE"),
                eq("test-session"),
                any(LocalDateTime.class)
        )).thenReturn("new-access");

        when(tokenProvider.generateRefreshToken(
                eq(1L),
                eq("refresh-test@example.com"),
                eq("test-session"),
                eq(session.getExpiresAt())
        )).thenReturn("new-refresh");

        when(tokenProvider.hashRefreshToken("new-refresh"))
                .thenReturn("new-hash");
    }

    @Test
    void returnsTokensWhenRotationSucceeds() {
        when(authSessionRepository.rotateRefreshTokenIfActive(
                eq("test-session"),
                eq(1L),
                eq("old-hash"),
                eq("new-hash"),
                any(LocalDateTime.class)
        )).thenReturn(true);

        AuthResult result = useCase.refreshToken("old-refresh");

        assertEquals("new-access", result.accessToken());
        assertEquals("new-refresh", result.refreshToken());
        assertEquals(Long.valueOf(1L), result.user().id());
    }

    @Test
    void rejectsRefreshIfSessionChangedBeforeUpdate() {
        // Phiên hợp lệ lúc đọc, nhưng bị từ chối lúc cập nhật.
        when(authSessionRepository.rotateRefreshTokenIfActive(
                eq("test-session"),
                eq(1L),
                eq("old-hash"),
                eq("new-hash"),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.refreshToken("old-refresh")
        );

        // Không được ghi lại toàn bộ phiên bằng cách lưu cũ.
        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void refreshedTokensNeverOutliveSession() {
        when(authSessionRepository.rotateRefreshTokenIfActive(
                eq("test-session"),
                eq(1L),
                eq("old-hash"),
                eq("new-hash"),
                any(LocalDateTime.class)
        )).thenReturn(true);

        AuthResult result =
                useCase.refreshToken("old-refresh");

        ArgumentCaptor<LocalDateTime> accessExpiryCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        ArgumentCaptor<LocalDateTime> refreshExpiryCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(tokenProvider).generateAccessToken(
                eq(1L),
                eq("refresh-test@example.com"),
                eq("ROLE_CANDIDATE"),
                eq("test-session"),
                accessExpiryCaptor.capture()
        );

        verify(tokenProvider).generateRefreshToken(
                eq(1L),
                eq("refresh-test@example.com"),
                eq("test-session"),
                refreshExpiryCaptor.capture()
        );

        assertFalse(
                accessExpiryCaptor.getValue()
                        .isAfter(refreshExpiryCaptor.getValue())
        );

        assertTrue(result.expiresIn() >= 0);
        assertTrue(result.expiresIn() <= 3600);
    }

}