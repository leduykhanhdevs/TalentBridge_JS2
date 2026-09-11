package vn.talentbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.talentbridge.core.application.port.in.*;
import vn.talentbridge.core.application.port.out.*;
import vn.talentbridge.core.application.usecase.*;

/**
 * Composition Root: Wires adapters into use cases via pure dependency injection.
 * As taught in Clean/Hexagonal Architecture (Slide Day 2 - 3).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUseCase registerUseCase(UserRepositoryPort userRepository,
                                           PasswordEncoderPort passwordEncoder,
                                           TokenProviderPort tokenProvider,
                                           @Value("${talentbridge.jwt.expiration-ms:86400000}") long expirationMs) {
        return new RegisterUseCaseImpl(userRepository, passwordEncoder, tokenProvider, expirationMs);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepository,
                                     PasswordEncoderPort passwordEncoder,
                                     TokenProviderPort tokenProvider,
                                     @Value("${talentbridge.jwt.expiration-ms:86400000}") long expirationMs) {
        return new LoginUseCaseImpl(userRepository, passwordEncoder, tokenProvider, expirationMs);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(UserRepositoryPort userRepository,
                                                   TokenProviderPort tokenProvider,
                                                   @Value("${talentbridge.jwt.expiration-ms:86400000}") long expirationMs) {
        return new RefreshTokenUseCaseImpl(userRepository, tokenProvider, expirationMs);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepositoryPort userRepository) {
        return new GetCurrentUserUseCaseImpl(userRepository);
    }

    @Bean
    public AdminManagementUseCase adminManagementUseCase(UserRepositoryPort userRepository,
                                                         CompanyRepositoryPort companyRepository,
                                                         JobRepositoryPort jobRepository) {
        return new AdminManagementUseCaseImpl(userRepository, companyRepository, jobRepository);
    }
}
