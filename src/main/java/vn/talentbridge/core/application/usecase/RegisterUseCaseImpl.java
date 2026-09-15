package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.RegisterUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.EmailAlreadyUsedException;
import vn.talentbridge.core.domain.model.AuthSession;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Recruiter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UserRepositoryPort userRepository;
    private final RecruiterRepositoryPort recruiterRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;
    private final long tokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public RegisterUseCaseImpl(
            UserRepositoryPort userRepository,
            RecruiterRepositoryPort recruiterRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            long tokenExpirationMs,
            long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.recruiterRepository = recruiterRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authSessionRepository = authSessionRepository;
        this.tokenExpirationMs = tokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
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

        if (roleName == RoleName.ROLE_RECRUITER) {
            Recruiter recruiter = new Recruiter();
            recruiter.setUser(savedUser);
            recruiter.setCompany(null);
            String position = command.position() != null && !command.position().isBlank()
                    ? command.position().trim()
                    : "Recruiter";
            recruiter.setPosition(position);
            recruiter.setCreatedAt(LocalDateTime.now());

            recruiterRepository.save(recruiter);
        }
        LocalDateTime now = LocalDateTime.now();
        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenProvider.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                roleName.name(),
                sessionId
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                savedUser.getId(),
                savedUser.getEmail(),
                sessionId
        );

        AuthSession authSession = new AuthSession(
                null,
                sessionId,
                savedUser.getId(),
                tokenProvider.hashRefreshToken(refreshToken),
                now,
                now.plus(Duration.ofMillis(refreshTokenExpirationMs)),
                null
        );

        authSessionRepository.save(authSession);

        return AuthResult.of(accessToken, refreshToken, tokenExpirationMs / 1000, UserResult.from(savedUser));
    }
}