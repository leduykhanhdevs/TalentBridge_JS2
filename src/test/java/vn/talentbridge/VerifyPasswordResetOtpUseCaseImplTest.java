package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.VerifyPasswordResetOtpUseCaseImpl;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyPasswordResetOtpUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordResetTokenRepositoryPort tokenRepository;

    @Mock
    private PasswordResetTokenGeneratorPort tokenGenerator;

    private VerifyPasswordResetOtpUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new VerifyPasswordResetOtpUseCaseImpl(
                userRepository,
                tokenRepository,
                tokenGenerator
        );
    }

    @Test
    void validOtpIsConsumedAndReturnsShortLivedResetGrant() {
        User user = user(10L);
        PasswordResetToken otp = otpToken(21L, 10L, "stored-otp-hash");
        when(userRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(user));
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(
                eq(10L),
                eq(PasswordResetToken.Purpose.OTP),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(otp));
        when(tokenGenerator.hashToken("password-reset-otp:10:21:123456"))
                .thenReturn("stored-otp-hash");
        when(tokenRepository.markUsedIfUsable(
                eq("stored-otp-hash"),
                eq(PasswordResetToken.Purpose.OTP),
                any(LocalDateTime.class)
        )).thenReturn(true);
        when(tokenGenerator.generateToken()).thenReturn("reset-grant");
        when(tokenGenerator.hashToken("reset-grant")).thenReturn("grant-hash");

        Optional<String> result = useCase.verifyOtp("candidate@test.com", "123456");

        assertEquals(Optional.of("reset-grant"), result);
        verify(tokenRepository).invalidateUnusedByUserId(eq(10L), any(LocalDateTime.class));
        ArgumentCaptor<PasswordResetToken> grantCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(grantCaptor.capture());
        PasswordResetToken grant = grantCaptor.getValue();
        assertEquals(PasswordResetToken.Purpose.RESET_GRANT, grant.getPurpose());
        assertEquals("grant-hash", grant.getTokenHash());
        assertEquals(5, Duration.between(grant.getCreatedAt(), grant.getExpiresAt()).toMinutes());
        verify(tokenRepository, never()).incrementFailedAttempts(anyLong(), any(), anyInt());
    }

    @Test
    void wrongOtpRecordsAttemptAndReturnsRetryableFailure() {
        when(userRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(user(10L)));
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(
                eq(10L),
                eq(PasswordResetToken.Purpose.OTP),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(otpToken(21L, 10L, "stored-otp-hash")));
        when(tokenGenerator.hashToken("password-reset-otp:10:21:000000"))
                .thenReturn("wrong-otp-hash");
        when(tokenRepository.incrementFailedAttempts(
                eq(21L),
                any(LocalDateTime.class),
                eq(5)
        )).thenReturn(true);

        assertTrue(useCase.verifyOtp("candidate@test.com", "000000").isEmpty());

        verify(tokenRepository).incrementFailedAttempts(eq(21L), any(), eq(5));
        verify(tokenRepository).markUsedIfAttemptLimitReached(eq(21L), any(), eq(5));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void unknownEmailGetsSameInvalidOtpResultWithoutRevealingAccount() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertTrue(useCase.verifyOtp("unknown@test.com", "123456").isEmpty());

        verifyNoInteractions(tokenRepository, tokenGenerator);
    }

    @Test
    void missingActiveOtpCannotCreateResetGrant() {
        when(userRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(user(10L)));
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(
                eq(10L),
                eq(PasswordResetToken.Purpose.OTP),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        assertTrue(useCase.verifyOtp("candidate@test.com", "123456").isEmpty());

        verifyNoInteractions(tokenGenerator);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("candidate@test.com");
        return user;
    }

    private PasswordResetToken otpToken(Long id, Long userId, String hash) {
        LocalDateTime now = LocalDateTime.now();
        return new PasswordResetToken(
                id,
                userId,
                hash,
                now.minusMinutes(1),
                now.plusMinutes(14),
                null,
                PasswordResetToken.Purpose.OTP,
                0
        );
    }
}
