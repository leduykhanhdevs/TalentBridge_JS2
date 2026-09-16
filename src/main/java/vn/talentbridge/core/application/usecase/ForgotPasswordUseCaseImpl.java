package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.ForgotPasswordUseCase;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
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
    private final String resetPasswordUrl;

    public ForgotPasswordUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            PasswordResetEmailPort emailPort,
            long tokenExpirationMinutes,
            String resetPasswordUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailPort = emailPort;
        this.tokenExpirationMinutes = tokenExpirationMinutes;
        this.resetPasswordUrl = resetPasswordUrl;
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
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

        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                user.getId(),
                tokenHash,
                now,
                expiresAt,
                null
        );

        tokenRepository.save(resetToken);

        String resetLink = resetPasswordUrl
                + "?token="
                + rawToken;

        emailPort.sendPasswordResetEmail(
                user.getEmail(),
                resetLink,
                expiresAt
        );
    }
}