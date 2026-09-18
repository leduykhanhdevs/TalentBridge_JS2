package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ChangePasswordCommand;
import vn.talentbridge.core.application.port.in.ChangePasswordUseCase;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.User;

public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public ChangePasswordUseCaseImpl(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordCommand command) {
        if (command.currentPassword() == null || command.currentPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không được để trống");
        }
        if (command.newPassword() == null || command.newPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản người dùng", userId));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }
}
