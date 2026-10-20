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
                true
        );
    }

    @Test
    void registeredEmailCreatesExpiringTokenAndSendsEmail() {
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));
        when(tokenGenerator.generateOtp()).thenReturn("123456");
        when(tokenGenerator.generateToken()).thenReturn("temporary-token");
        when(tokenGenerator.hashToken("temporary-token"))
                .thenReturn("temporary-hash");
        when(tokenGenerator.hashToken("password-reset-otp:1:77:123456"))
                .thenReturn("otp-hash");
        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> {
                    PasswordResetToken saved = invocation.getArgument(0);
                    saved.setId(77L);
                    return saved;
                });

        useCase.requestPasswordReset("user@test.com");

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepository).invalidateUnusedByUserId(
                eq(1L),
                any(LocalDateTime.class)
        );
        verify(tokenRepository, times(2)).save(captor.capture());

        PasswordResetToken savedToken = captor.getAllValues().get(1);

        assertEquals(1L, savedToken.getUserId());
        assertEquals("otp-hash", savedToken.getTokenHash());
        assertEquals(PasswordResetToken.Purpose.OTP, savedToken.getPurpose());
        assertEquals(0, savedToken.getFailedAttempts());
        assertNull(savedToken.getUsedAt());
        assertEquals(
                15,
                Duration.between(
                        savedToken.getCreatedAt(),
                        savedToken.getExpiresAt()
                ).toMinutes()
        );

        verify(emailPort).sendPasswordResetOtp(
                eq("user@test.com"),
                eq("123456"),
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

    @Test
    void disabledMailDeliveryFailsBeforeLookingUpAccount() {
        ForgotPasswordUseCaseImpl mailDisabledUseCase =
                new ForgotPasswordUseCaseImpl(
                        userRepository,
                        tokenRepository,
                        tokenGenerator,
                        emailPort,
                        15L,
                        false
                );

        assertThrows(
                vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException.class,
                () -> mailDisabledUseCase.requestPasswordReset("user@test.com")
        );

        verifyNoInteractions(
                userRepository,
                tokenRepository,
                tokenGenerator,
                emailPort
        );
    }
}
