package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.ResetPasswordUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.InvalidPasswordResetTokenException;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;

public class ResetPasswordUseCaseImpl
        implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordResetTokenGeneratorPort tokenGenerator;
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final AuthSessionRepositoryPort authSessionRepository;

    public ResetPasswordUseCaseImpl(
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            AuthSessionRepositoryPort authSessionRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    public void resetPassword(
            String rawToken,
            String newPassword
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidPasswordResetTokenException();
        }

        LocalDateTime now = LocalDateTime.now();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(
                        InvalidPasswordResetTokenException::new
                );

        if (!resetToken.isUsableAt(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = userRepository
                .findById(resetToken.getUserId())
                .orElseThrow(
                        InvalidPasswordResetTokenException::new
                );

        boolean markedUsed = tokenRepository.markUsedIfUsable(
                tokenHash,
                now
        );

        if (!markedUsed) {
            throw new InvalidPasswordResetTokenException();
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );
        user.setUpdatedAt(now);
        userRepository.save(user);

        authSessionRepository.revokeAllActiveSessions(
                user.getId(),
                now
        );

        tokenRepository.invalidateUnusedByUserId(
                user.getId(),
                now
        );
    }
}