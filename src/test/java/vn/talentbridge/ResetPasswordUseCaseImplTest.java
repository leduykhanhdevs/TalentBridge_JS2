package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.ResetPasswordUseCaseImpl;
import vn.talentbridge.core.domain.exception.InvalidPasswordResetTokenException;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private PasswordResetTokenRepositoryPort tokenRepository;

    @Mock
    private PasswordResetTokenGeneratorPort tokenGenerator;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private AuthSessionRepositoryPort authSessionRepository;

    private ResetPasswordUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCaseImpl(
                tokenRepository,
                tokenGenerator,
                userRepository,
                passwordEncoder,
                authSessionRepository
        );
    }

    @Test
    void validTokenChangesPasswordAndRevokesAllSessions() {
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken token = new PasswordResetToken(
                1L,
                10L,
                "hashed-token",
                now.minusMinutes(1),
                now.plusMinutes(10),
                null
        );

        User user = new User();
        user.setId(10L);
        user.setPasswordHash("old-password");

        when(tokenGenerator.hashToken("raw-token"))
                .thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));
        when(tokenRepository.markUsedIfUsable(
                eq("hashed-token"),
                any(LocalDateTime.class)
        )).thenReturn(true);
        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123"))
                .thenReturn("encoded-new-password");

        useCase.resetPassword("raw-token", "NewPassword123");

        assertEquals(
                "encoded-new-password",
                user.getPasswordHash()
        );

        verify(userRepository).save(user);
        verify(authSessionRepository).revokeAllActiveSessions(
                eq(10L),
                any(LocalDateTime.class)
        );
        verify(tokenRepository).invalidateUnusedByUserId(
                eq(10L),
                any(LocalDateTime.class)
        );
    }

    @Test
    void expiredTokenIsRejected() {
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken expiredToken = new PasswordResetToken(
                1L,
                10L,
                "expired-hash",
                now.minusMinutes(20),
                now.minusMinutes(5),
                null
        );

        when(tokenGenerator.hashToken("expired-token"))
                .thenReturn("expired-hash");
        when(tokenRepository.findByTokenHash("expired-hash"))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> useCase.resetPassword(
                        "expired-token",
                        "NewPassword123"
                )
        );

        verify(tokenRepository, never())
                .markUsedIfUsable(anyString(), any());
        verifyNoInteractions(
                userRepository,
                passwordEncoder,
                authSessionRepository
        );
    }

    @Test
    void alreadyUsedTokenIsRejected() {
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken usedToken = new PasswordResetToken(
                1L,
                10L,
                "used-hash",
                now.minusMinutes(5),
                now.plusMinutes(10),
                now.minusMinutes(1)
        );

        when(tokenGenerator.hashToken("used-token"))
                .thenReturn("used-hash");
        when(tokenRepository.findByTokenHash("used-hash"))
                .thenReturn(Optional.of(usedToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> useCase.resetPassword(
                        "used-token",
                        "NewPassword123"
                )
        );

        verifyNoInteractions(
                userRepository,
                passwordEncoder,
                authSessionRepository
        );
    }
}