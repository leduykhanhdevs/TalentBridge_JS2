package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.ForgotPasswordUseCaseImpl;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordResetTokenRepositoryPort tokenRepository;

    @Mock
    private PasswordResetTokenGeneratorPort tokenGenerator;

    @Mock
    private PasswordResetEmailPort emailPort;

    private ForgotPasswordUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ForgotPasswordUseCaseImpl(
                userRepository,
                tokenRepository,
                tokenGenerator,
                emailPort,
                15L,
                "http://localhost:3000/reset-password"
        );
    }

    @Test
    void registeredEmailCreatesExpiringTokenAndSendsEmail() {
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));
        when(tokenGenerator.generateToken()).thenReturn("raw-token");
        when(tokenGenerator.hashToken("raw-token"))
                .thenReturn("hashed-token");

        useCase.requestPasswordReset("user@test.com");

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepository).invalidateUnusedByUserId(
                eq(1L),
                any(LocalDateTime.class)
        );
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();

        assertEquals(1L, savedToken.getUserId());
        assertEquals("hashed-token", savedToken.getTokenHash());
        assertNull(savedToken.getUsedAt());
        assertEquals(
                15,
                Duration.between(
                        savedToken.getCreatedAt(),
                        savedToken.getExpiresAt()
                ).toMinutes()
        );

        verify(emailPort).sendPasswordResetEmail(
                eq("user@test.com"),
                eq("http://localhost:3000/reset-password?token=raw-token"),
                eq(savedToken.getExpiresAt())
        );
    }

    @Test
    void unknownEmailReturnsNormallyWithoutRevealingAccount() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> useCase.requestPasswordReset("unknown@test.com")
        );

        verifyNoInteractions(
                tokenRepository,
                tokenGenerator,
                emailPort
        );
    }
}