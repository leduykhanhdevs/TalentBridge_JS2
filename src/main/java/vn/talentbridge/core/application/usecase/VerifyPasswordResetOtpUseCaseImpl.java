package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.VerifyPasswordResetOtpUseCase;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

public class VerifyPasswordResetOtpUseCaseImpl
        implements VerifyPasswordResetOtpUseCase {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long RESET_GRANT_EXPIRATION_MINUTES = 5;

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordResetTokenGeneratorPort tokenGenerator;

    public VerifyPasswordResetOtpUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Optional<String> verifyOtp(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOptional = userRepository.findByEmail(email.trim());
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetToken> otpOptional = tokenRepository
                .findLatestActiveByUserIdAndPurpose(
                        user.getId(),
                        PasswordResetToken.Purpose.OTP,
                        now
                );
        if (otpOptional.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken otpRecord = otpOptional.get();
        String expectedHash = tokenGenerator.hashToken(
                buildOtpHashInput(user.getId(), otpRecord.getId(), otp)
        );
        if (!constantTimeEquals(otpRecord.getTokenHash(), expectedHash)) {
            recordFailedAttempt(otpRecord, now);
            return Optional.empty();
        }

        if (!tokenRepository.markUsedIfUsable(
                otpRecord.getTokenHash(),
                PasswordResetToken.Purpose.OTP,
                now
        )) {
            return Optional.empty();
        }

        tokenRepository.invalidateUnusedByUserId(user.getId(), now);

        String resetGrant = tokenGenerator.generateToken();
        tokenRepository.save(new PasswordResetToken(
                null,
                user.getId(),
                tokenGenerator.hashToken(resetGrant),
                now,
                now.plusMinutes(RESET_GRANT_EXPIRATION_MINUTES),
                null,
                PasswordResetToken.Purpose.RESET_GRANT,
                0
        ));

        return Optional.of(resetGrant);
    }

    private void recordFailedAttempt(PasswordResetToken otpRecord, LocalDateTime now) {
        boolean recorded = tokenRepository.incrementFailedAttempts(
                otpRecord.getId(),
                now,
                MAX_FAILED_ATTEMPTS
        );
        if (recorded) {
            tokenRepository.markUsedIfAttemptLimitReached(
                    otpRecord.getId(),
                    now,
                    MAX_FAILED_ATTEMPTS
            );
        }
    }

    private String buildOtpHashInput(Long userId, Long tokenId, String otp) {
        return "password-reset-otp:" + userId + ":" + tokenId + ":" + otp;
    }

    private boolean constantTimeEquals(String left, String right) {
        return left != null
                && right != null
                && java.security.MessageDigest.isEqual(
                        left.getBytes(StandardCharsets.US_ASCII),
                        right.getBytes(StandardCharsets.US_ASCII)
                );
    }
}
