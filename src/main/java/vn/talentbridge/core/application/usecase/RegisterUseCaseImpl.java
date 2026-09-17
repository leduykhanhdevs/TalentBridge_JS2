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

import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.InvalidRoleException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Recruiter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UserRepositoryPort userRepository;
    private final RecruiterRepositoryPort recruiterRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;
    private final long tokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public RegisterUseCaseImpl(
            UserRepositoryPort userRepository,
            RecruiterRepositoryPort recruiterRepository,
            CandidateRepositoryPort candidateRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            long tokenExpirationMs,
            long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.recruiterRepository = recruiterRepository;
        this.candidateRepository = candidateRepository;
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

        if (command.role() == null || command.role().isBlank()) {
            throw new InvalidRoleException(command.role());
        }

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(command.role().trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException(command.role());
        }

        if (roleName != RoleName.ROLE_CANDIDATE && roleName != RoleName.ROLE_RECRUITER) {
            throw new InvalidRoleException(command.role());
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
        } else if (roleName == RoleName.ROLE_CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(savedUser);
            candidate.setCreatedAt(LocalDateTime.now());
            candidate.setUpdatedAt(LocalDateTime.now());

            candidateRepository.save(candidate);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionExpiresAt = now.plus(
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        LocalDateTime configuredAccessExpiresAt = now.plus(
                Duration.ofMillis(tokenExpirationMs)
        );

        LocalDateTime accessTokenExpiresAt =
                configuredAccessExpiresAt.isBefore(sessionExpiresAt)
                        ? configuredAccessExpiresAt
                        : sessionExpiresAt;

        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenProvider.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                roleName.name(),
                sessionId,
                accessTokenExpiresAt
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                savedUser.getId(),
                savedUser.getEmail(),
                sessionId,
                sessionExpiresAt
        );

        AuthSession authSession = new AuthSession(
                null,
                sessionId,
                savedUser.getId(),
                tokenProvider.hashRefreshToken(refreshToken),
                now,
                sessionExpiresAt,
                null
        );

        authSessionRepository.save(authSession);

        long expiresInSeconds = Math.max(
                0L,
                Duration.between(
                        now,
                        accessTokenExpiresAt
                ).toSeconds()
        );

        return AuthResult.of(
                accessToken,
                refreshToken,
                expiresInSeconds,
                UserResult.from(savedUser)
        );
    }
}