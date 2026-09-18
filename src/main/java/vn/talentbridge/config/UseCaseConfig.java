package vn.talentbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.talentbridge.core.application.port.in.*;
import vn.talentbridge.core.application.port.out.*;
import vn.talentbridge.core.application.usecase.*;

/**
 * Composition Root: Wires adapters into use cases via pure dependency
 * injection.
 * As taught in Clean/Hexagonal Architecture (Slide Day 2 - 3).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepositoryPort userRepository,
            RecruiterRepositoryPort recruiterRepository,
            CandidateRepositoryPort candidateRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            @Value("${talentbridge.jwt.expiration-ms:86400000}")
            long expirationMs,
            @Value("${talentbridge.jwt.refresh-expiration-ms:604800000}")
            long refreshExpirationMs
    ) {
        RegisterUseCase core = new RegisterUseCaseImpl(
                userRepository,
                recruiterRepository,
                candidateRepository,
                passwordEncoder,
                tokenProvider,
                authSessionRepository,
                expirationMs,
                refreshExpirationMs
        );
        return new TransactionalRegisterUseCase(core);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            LoginAttemptTrackerPort loginAttemptTracker,
            @Value("${talentbridge.jwt.expiration-ms:86400000}")
            long expirationMs,
            @Value("${talentbridge.jwt.refresh-expiration-ms:604800000}")
            long refreshExpirationMs
    ) {
        return new LoginUseCaseImpl(
                userRepository,
                passwordEncoder,
                tokenProvider,
                authSessionRepository,
                loginAttemptTracker,
                expirationMs,
                refreshExpirationMs
        );
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            UserRepositoryPort userRepository,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            @Value("${talentbridge.jwt.expiration-ms:86400000}")
            long expirationMs
    ) {
        return new RefreshTokenUseCaseImpl(
                userRepository,
                tokenProvider,
                authSessionRepository,
                expirationMs
        );
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository
    ) {
        return new LogoutUseCaseImpl(
                tokenProvider,
                authSessionRepository
        );
    }

    @Bean
    public ForgotPasswordUseCase forgotPasswordUseCase(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            PasswordResetEmailPort emailPort,
            @Value(
                    "${talentbridge.password-reset."
                            + "token-expiration-minutes:15}"
            )
            long tokenExpirationMinutes,
            @Value(
                    "${talentbridge.password-reset."
                            + "reset-password-url:"
                            + "http://localhost:5173/reset-password}"
            )
            String resetPasswordUrl
    ) {
        return new ForgotPasswordUseCaseImpl(
                userRepository,
                tokenRepository,
                tokenGenerator,
                emailPort,
                tokenExpirationMinutes,
                resetPasswordUrl
        );
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(
            PasswordResetTokenRepositoryPort tokenRepository,
            PasswordResetTokenGeneratorPort tokenGenerator,
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            AuthSessionRepositoryPort authSessionRepository
    ) {
        return new ResetPasswordUseCaseImpl(
                tokenRepository,
                tokenGenerator,
                userRepository,
                passwordEncoder,
                authSessionRepository
        );
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder
    ) {
        return new ChangePasswordUseCaseImpl(userRepository, passwordEncoder);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepositoryPort userRepository) {
        return new GetCurrentUserUseCaseImpl(userRepository);
    }

    @Bean
    public AdminManagementUseCase adminManagementUseCase(UserRepositoryPort userRepository,
            CompanyRepositoryPort companyRepository,
            JobRepositoryPort jobRepository,
            RecruiterRepositoryPort recruiterRepository,
            CandidateRepositoryPort candidateRepository) {
        return new AdminManagementUseCaseImpl(userRepository, companyRepository, jobRepository, recruiterRepository,
                candidateRepository);
    }

    @Bean
    public GetRecruiterProfileUseCase getRecruiterProfileUseCase(
            RecruiterRepositoryPort recruiterRepository) {
        return new GetRecruiterProfileUseCaseImpl(recruiterRepository);
    }

    @Bean
    public UpdateRecruiterProfileUseCase updateRecruiterProfileUseCase(
            RecruiterRepositoryPort recruiterRepository,
            UserRepositoryPort userRepository) {
        return new UpdateRecruiterProfileUseCaseImpl(
                recruiterRepository,
                userRepository);
    }

    @Bean
    public RequestCreateCompanyUseCase requestCreateCompanyUseCase(
            RecruiterRepositoryPort recruiterRepository,
            CompanyRepositoryPort companyRepository) {
        return new RequestCreateCompanyUseCaseImpl(recruiterRepository, companyRepository);
    }

    @Bean
    public GetCandidateProfileUseCase getCandidateProfileUseCase(
            CandidateRepositoryPort candidateRepository,
            UserRepositoryPort userRepository) {
        return new GetCandidateProfileUseCaseImpl(candidateRepository, userRepository);
    }

    @Bean
    public UpdateCandidateProfileUseCase updateCandidateProfileUseCase(
            CandidateRepositoryPort candidateRepository,
            UserRepositoryPort userRepository) {
        return new UpdateCandidateProfileUseCaseImpl(candidateRepository, userRepository);
    }

    @Bean
    public WorkExperienceUseCase workExperienceUseCase(
            CandidateRepositoryPort candidateRepository,
            WorkExperienceRepositoryPort workExperienceRepository) {
        return new WorkExperienceUseCaseImpl(candidateRepository, workExperienceRepository);
    }

    @Bean
    public CandidateSkillUseCase candidateSkillUseCase(
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository,
            SkillRepositoryPort skillRepository) {
        return new CandidateSkillUseCaseImpl(candidateRepository, candidateSkillRepository, skillRepository);
    }

    @Bean
    public ResumeUseCase resumeUseCase(
            ResumeRepositoryPort resumeRepository,
            CandidateRepositoryPort candidateRepository,
            UserRepositoryPort userRepository,
            FileStoragePort fileStoragePort) {
        return new ResumeUseCaseImpl(resumeRepository, candidateRepository, userRepository, fileStoragePort);
    }
}
