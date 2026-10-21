package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;

public class ForgotPasswordUseCaseImpl
        implements ForgotPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordResetTokenGeneratorPort tokenGenerator;
    private final PasswordResetEmailPort emailPort;
    private final long tokenExpirationMinutes;
    private final boolean mailEnabled;

    public ForgotPasswordUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            PasswordResetEmailPort emailPort,
            long tokenExpirationMinutes,
            boolean mailEnabled
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailPort = emailPort;
        this.tokenExpirationMinutes = tokenExpirationMinutes;
        this.mailEnabled = mailEnabled;
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        if (!mailEnabled) {
            throw new PasswordResetMailUnavailableException();
        }

        userRepository.findByEmail(email.trim())
                .ifPresent(this::createAndSendResetToken);
    }

    private void createAndSendResetToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(
                tokenExpirationMinutes
        );

        tokenRepository.invalidateUnusedByUserId(
                user.getId(),
                now
        );

        String otp = tokenGenerator.generateOtp();
        String temporaryToken = tokenGenerator.generateToken();

        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                user.getId(),
                tokenGenerator.hashToken(temporaryToken),
                now,
                expiresAt,
                null,
                PasswordResetToken.Purpose.OTP,
                0
        );

        PasswordResetToken savedToken = tokenRepository.save(resetToken);
        savedToken.setTokenHash(tokenGenerator.hashToken(
                "password-reset-otp:"
                        + user.getId()
                        + ":"
                        + savedToken.getId()
                        + ":"
                        + otp
        ));
        tokenRepository.save(savedToken);

        emailPort.sendPasswordResetOtp(
                user.getEmail(),
                otp,
                expiresAt
        );
    }
}
