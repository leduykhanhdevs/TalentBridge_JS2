package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.RegisterUseCase;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.EmailAlreadyUsedException;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDateTime;

public class RegisterUseCaseImpl implements RegisterUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final long tokenExpirationMs;

    public RegisterUseCaseImpl(UserRepositoryPort userRepository,
                               PasswordEncoderPort passwordEncoder,
                               TokenProviderPort tokenProvider,
                               long tokenExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    @Override
    public AuthResult register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyUsedException(command.email());
        }

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(command.role());
        } catch (Exception e) {
            roleName = RoleName.ROLE_CANDIDATE;
        }

        User user = new User();
        user.setEmail(command.email());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setFullName(command.fullName());
        user.setPhoneNumber(command.phoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Role role = new Role(null, roleName, roleName.name());
        user.addRole(role);

        User savedUser = userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), roleName.name());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return AuthResult.of(accessToken, refreshToken, tokenExpirationMs / 1000, UserResult.from(savedUser));
    }
}