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
    // hiếu
    @Bean
    public RegisterUseCase registerUseCase(
            UserRepositoryPort userRepository,
            RecruiterRepositoryPort recruiterRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
            @Value("${talentbridge.jwt.expiration-ms:86400000}")
            long expirationMs,
            @Value("${talentbridge.jwt.refresh-expiration-ms:604800000}")
            long refreshExpirationMs
    ) {
        return new RegisterUseCaseImpl(
                userRepository,
                recruiterRepository,
                passwordEncoder,
                tokenProvider,
                authSessionRepository,
                expirationMs,
                refreshExpirationMs
        );
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider,
            AuthSessionRepositoryPort authSessionRepository,
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
}
