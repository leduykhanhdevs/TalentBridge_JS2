package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.in.ContactVerificationUseCase;
import vn.talentbridge.core.application.port.out.ContactVerificationDeliveryPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.ContactVerificationUseCaseImpl;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactVerificationUseCaseImplTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordResetTokenRepositoryPort tokenRepository;
    @Mock private PasswordResetTokenGeneratorPort tokenGenerator;
    @Mock private ContactVerificationDeliveryPort deliveryPort;

    private ContactVerificationUseCaseImpl useCase;
    private User user;

    @BeforeEach
    void setUp() {
        useCase = new ContactVerificationUseCaseImpl(userRepository, tokenRepository, tokenGenerator, deliveryPort);
        user = new User();
        user.setId(42L);
        user.setEmail("candidate@example.invalid");
        user.setPhoneNumber("0912345678");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
    }

    @Test
    void requestEmailOtpStoresOnlyPurposeScopedHashAndSendsToAccountEmail() {
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(eq(42L), eq(PasswordResetToken.Purpose.EMAIL_VERIFICATION), any()))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generateOtp()).thenReturn("123456");
        when(tokenGenerator.hashToken(anyString())).thenReturn("opaque-hash");

        useCase.requestCode(42L, ContactVerificationUseCase.Channel.EMAIL);

        ArgumentCaptor<PasswordResetToken> token = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(token.capture());
        assertThat(token.getValue().getPurpose()).isEqualTo(PasswordResetToken.Purpose.EMAIL_VERIFICATION);
        assertThat(token.getValue().getTokenHash()).isEqualTo("opaque-hash");
        verify(deliveryPort).sendEmailOtp(eq("candidate@example.invalid"), eq("123456"), any());
        verify(deliveryPort, never()).sendPhoneOtp(anyString(), anyString(), any());
    }

    @Test
    void successfulPhoneOtpVerifiesOnlyPhoneAndConsumesTheCode() {
        PasswordResetToken token = new PasswordResetToken(
                7L, 42L, "opaque-hash", LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(9), null, PasswordResetToken.Purpose.PHONE_VERIFICATION, 0
        );
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(eq(42L), eq(PasswordResetToken.Purpose.PHONE_VERIFICATION), any()))
                .thenReturn(Optional.of(token));
        when(tokenGenerator.hashToken(anyString())).thenReturn("opaque-hash");
        when(tokenRepository.markUsedIfUsable(eq("opaque-hash"), eq(PasswordResetToken.Purpose.PHONE_VERIFICATION), any()))
                .thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.verifyCode(42L, ContactVerificationUseCase.Channel.PHONE, "123456");

        assertThat(result.phoneVerified()).isTrue();
        assertThat(result.emailVerified()).isFalse();
        verify(tokenRepository).markUsedIfUsable(eq("opaque-hash"), eq(PasswordResetToken.Purpose.PHONE_VERIFICATION), any());
    }

    @Test
    void wrongOtpConsumesAttemptButDoesNotVerifyAccount() {
        PasswordResetToken token = new PasswordResetToken(
                7L, 42L, "expected-hash", LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(9), null, PasswordResetToken.Purpose.EMAIL_VERIFICATION, 0
        );
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(eq(42L), eq(PasswordResetToken.Purpose.EMAIL_VERIFICATION), any()))
                .thenReturn(Optional.of(token));
        when(tokenGenerator.hashToken(anyString())).thenReturn("wrong-hash");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.verifyCode(42L, ContactVerificationUseCase.Channel.EMAIL, "000000"));

        verify(tokenRepository).incrementFailedAttempts(eq(7L), any(), eq(5));
        verify(tokenRepository).markUsedIfAttemptLimitReached(eq(7L), any(), eq(5));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendWaitWindowPreventsRepeatedOtpRequests() {
        PasswordResetToken recent = new PasswordResetToken(
                7L, 42L, "opaque-hash", LocalDateTime.now().minusSeconds(20),
                LocalDateTime.now().plusMinutes(9), null, PasswordResetToken.Purpose.EMAIL_VERIFICATION, 0
        );
        when(tokenRepository.findLatestActiveByUserIdAndPurpose(eq(42L), eq(PasswordResetToken.Purpose.EMAIL_VERIFICATION), any()))
                .thenReturn(Optional.of(recent));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.requestCode(42L, ContactVerificationUseCase.Channel.EMAIL));

        verifyNoInteractions(deliveryPort);
        verify(tokenRepository, never()).save(any());
    }
}
