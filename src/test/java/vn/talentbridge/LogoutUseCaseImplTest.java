package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.usecase.LogoutUseCaseImpl;
import vn.talentbridge.core.domain.exception.InvalidSessionException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String SESSION_ID = "test-session";
    private static final Long USER_ID = 1L;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private AuthSessionRepositoryPort authSessionRepository;

    private LogoutUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCaseImpl(
                tokenProvider,
                authSessionRepository
        );
    }

    @Test
    void logoutRevokesTokenSession() {
        stubValidAccessToken();

        when(authSessionRepository.revokeActiveSession(
                eq(SESSION_ID),
                eq(USER_ID),
                any(LocalDateTime.class)
        )).thenReturn(true);

        assertDoesNotThrow(() -> useCase.logout(ACCESS_TOKEN));

        verify(authSessionRepository).revokeActiveSession(
                eq(SESSION_ID),
                eq(USER_ID),
                any(LocalDateTime.class)
        );
    }

    @Test
    void rejectsMissingToken() {
        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(null)
        );

        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(" ")
        );

        verifyNoInteractions(tokenProvider, authSessionRepository);
    }

    @Test
    void rejectsInvalidOrExpiredToken() {
        when(tokenProvider.validateToken(ACCESS_TOKEN))
                .thenReturn(false);

        InvalidSessionException exception = assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        assertEquals(40101, exception.getCode());
        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void rejectsRefreshToken() {
        when(tokenProvider.validateToken(ACCESS_TOKEN))
                .thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken(ACCESS_TOKEN))
                .thenReturn("refresh");

        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void rejectsMissingSessionOrUser() {
        when(tokenProvider.validateToken(ACCESS_TOKEN))
                .thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken(ACCESS_TOKEN))
                .thenReturn("access");
        when(tokenProvider.getSessionIdFromToken(ACCESS_TOKEN))
                .thenReturn(" ", SESSION_ID);
        when(tokenProvider.getUserIdFromToken(ACCESS_TOKEN))
                .thenReturn(USER_ID, (Long) null);

        // Lần đầu: thiếu sessionId.
        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        // Lần sau: có sessionId nhưng thiếu userId.
        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void rejectsTokenThatCannotBeRead() {
        when(tokenProvider.validateToken(ACCESS_TOKEN))
                .thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken(ACCESS_TOKEN))
                .thenThrow(new IllegalArgumentException(
                        "Token can no longer be read"
                ));

        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void rejectsSessionThatCannotBeRevoked() {
        stubValidAccessToken();

        when(authSessionRepository.revokeActiveSession(
                eq(SESSION_ID),
                eq(USER_ID),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertThrows(
                InvalidSessionException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );
    }

    @Test
    void propagatesPersistenceFailure() {
        stubValidAccessToken();

        IllegalStateException databaseFailure =
                new IllegalStateException("Database unavailable");

        when(authSessionRepository.revokeActiveSession(
                eq(SESSION_ID),
                eq(USER_ID),
                any(LocalDateTime.class)
        )).thenThrow(databaseFailure);

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> useCase.logout(ACCESS_TOKEN)
        );

        assertSame(databaseFailure, actual);
    }

    private void stubValidAccessToken() {
        when(tokenProvider.validateToken(ACCESS_TOKEN))
                .thenReturn(true);
        when(tokenProvider.getTokenTypeFromToken(ACCESS_TOKEN))
                .thenReturn("access");
        when(tokenProvider.getSessionIdFromToken(ACCESS_TOKEN))
                .thenReturn(SESSION_ID);
        when(tokenProvider.getUserIdFromToken(ACCESS_TOKEN))
                .thenReturn(USER_ID);
    }
}