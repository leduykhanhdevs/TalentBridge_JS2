package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.BootstrapAdminAccountUseCase;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class BootstrapAdminAccountUseCaseImpl implements BootstrapAdminAccountUseCase {
    private static final int MIN_PASSWORD_CODE_POINTS = 16;
    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_FULL_NAME_LENGTH = 100;

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public BootstrapAdminAccountUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean execute(String email, String rawPassword, String fullName) {
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);
        String normalizedFullName = normalizeFullName(fullName);

        var existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            if (existingUser.get().hasRole(RoleName.ROLE_ADMIN)) {
                return false;
            }
            throw new IllegalStateException(
                    "Admin bootstrap refused because the configured email belongs to a non-admin account."
            );
        }

        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setFullName(normalizedFullName);
        admin.setStatus(UserStatus.ACTIVE);
        admin.addRole(new Role(null, RoleName.ROLE_ADMIN, "Quản trị viên hệ thống"));
        userRepository.save(admin);
        return true;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Admin bootstrap email is required.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_EMAIL_LENGTH || !normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Admin bootstrap email is invalid.");
        }
        return normalized;
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.codePointCount(0, rawPassword.length()) < MIN_PASSWORD_CODE_POINTS
                || rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "Admin bootstrap password must contain at least 16 characters and fit the BCrypt 72-byte limit."
            );
        }
    }

    private String normalizeFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "TalentBridge Administrator";
        }
        String normalized = fullName.trim();
        if (normalized.length() > MAX_FULL_NAME_LENGTH) {
            throw new IllegalArgumentException("Admin bootstrap full name must be at most 100 characters.");
        }
        return normalized;
    }
}
