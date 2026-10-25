package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ContactVerificationStatusResult;
import vn.talentbridge.core.application.port.in.ContactVerificationUseCase;
import vn.talentbridge.core.application.port.out.ContactVerificationDeliveryPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;
import vn.talentbridge.core.application.port.out.PasswordResetTokenRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.model.PasswordResetToken;
import vn.talentbridge.core.domain.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;

public class ContactVerificationUseCaseImpl implements ContactVerificationUseCase {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_RESEND_SECONDS = 60;
    private static final int OTP_ATTEMPT_LIMIT = 5;

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordResetTokenGeneratorPort tokenGenerator;
    private final ContactVerificationDeliveryPort deliveryPort;

    public ContactVerificationUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            ContactVerificationDeliveryPort deliveryPort
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.deliveryPort = deliveryPort;
    }

    @Override
    public ContactVerificationStatusResult getStatus(Long userId) {
        return toResult(findUser(userId));
    }

    @Override
    public void requestCode(Long userId, Channel channel) {
        User user = findUser(userId);
        PasswordResetToken.Purpose purpose = purposeFor(channel);
        if (isVerified(user, channel)) {
            return;
        }

        String destination = destinationFor(user, channel);
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.findLatestActiveByUserIdAndPurpose(userId, purpose, now)
                .filter(token -> token.getCreatedAt() != null
                        && Duration.between(token.getCreatedAt(), now).getSeconds() < OTP_RESEND_SECONDS)
                .ifPresent(token -> {
                    throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi yêu cầu mã mới.");
                });

        tokenRepository.invalidateUnusedByUserIdAndPurpose(userId, purpose, now);
        String otp = tokenGenerator.generateOtp();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        String tokenHash = hashOtp(userId, purpose, otp);
        tokenRepository.save(new PasswordResetToken(
                null, userId, tokenHash, now, expiresAt, null, purpose, 0
        ));

        try {
            if (channel == Channel.EMAIL) {
                deliveryPort.sendEmailOtp(destination, otp, expiresAt);
            } else {
                deliveryPort.sendPhoneOtp(destination, otp, expiresAt);
            }
        } catch (RuntimeException exception) {
            tokenRepository.invalidateUnusedByUserIdAndPurpose(userId, purpose, LocalDateTime.now());
            throw exception;
        }
    }

    @Override
    public ContactVerificationStatusResult verifyCode(Long userId, Channel channel, String otp) {
        if (otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException("Mã xác minh phải gồm 6 chữ số.");
        }
        User user = findUser(userId);
        if (isVerified(user, channel)) {
            return toResult(user);
        }

        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken.Purpose purpose = purposeFor(channel);
        PasswordResetToken token = tokenRepository.findLatestActiveByUserIdAndPurpose(userId, purpose, now)
                .orElseThrow(() -> new IllegalArgumentException("Mã xác minh không hợp lệ hoặc đã hết hạn. Hãy yêu cầu mã mới."));
        String expectedHash = hashOtp(userId, purpose, otp);
        if (!MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.US_ASCII),
                token.getTokenHash().getBytes(StandardCharsets.US_ASCII)
        )) {
            tokenRepository.incrementFailedAttempts(token.getId(), now, OTP_ATTEMPT_LIMIT);
            tokenRepository.markUsedIfAttemptLimitReached(token.getId(), now, OTP_ATTEMPT_LIMIT);
            throw new IllegalArgumentException("Mã xác minh không đúng.");
        }

        if (!tokenRepository.markUsedIfUsable(token.getTokenHash(), purpose, now)) {
            throw new IllegalArgumentException("Mã xác minh không hợp lệ hoặc đã hết hạn. Hãy yêu cầu mã mới.");
        }

        if (channel == Channel.EMAIL) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }
        return toResult(userRepository.save(user));
    }

    private User findUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
    }

    private PasswordResetToken.Purpose purposeFor(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Kênh xác minh không hợp lệ.");
        }
        return channel == Channel.EMAIL
                ? PasswordResetToken.Purpose.EMAIL_VERIFICATION
                : PasswordResetToken.Purpose.PHONE_VERIFICATION;
    }

    private boolean isVerified(User user, Channel channel) {
        return channel == Channel.EMAIL ? user.isEmailVerified() : user.isPhoneVerified();
    }

    private String destinationFor(User user, Channel channel) {
        String destination = channel == Channel.EMAIL ? user.getEmail() : user.getPhoneNumber();
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(channel == Channel.EMAIL
                    ? "Tài khoản chưa có email để xác minh."
                    : "Tài khoản chưa có số điện thoại để xác minh.");
        }
        return destination.trim();
    }

    private String hashOtp(Long userId, PasswordResetToken.Purpose purpose, String otp) {
        return tokenGenerator.hashToken("contact-verification:" + purpose.name() + ":" + userId + ":" + otp);
    }

    private ContactVerificationStatusResult toResult(User user) {
        return new ContactVerificationStatusResult(
                user.isEmailVerified(),
                user.isPhoneVerified(),
                maskEmail(user.getEmail()),
                maskPhone(user.getPhoneNumber())
        );
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "";
        int at = email.indexOf('@');
        if (at < 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String digits = phone.replaceAll("\\D", "");
        return digits.length() <= 4 ? "***" : "***" + digits.substring(digits.length() - 4);
    }
}
