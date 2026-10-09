package vn.talentbridge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import vn.talentbridge.core.application.port.in.BootstrapAdminAccountUseCase;

@Slf4j
@Configuration
@Profile("prod")
public class AdminBootstrapConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "talentbridge.admin-bootstrap",
            name = "enabled",
            havingValue = "true"
    )
    ApplicationRunner bootstrapAdminAccount(
            BootstrapAdminAccountUseCase useCase,
            @Value("${talentbridge.admin-bootstrap.email:}") String email,
            @Value("${talentbridge.admin-bootstrap.password:}") String password,
            @Value("${talentbridge.admin-bootstrap.full-name:TalentBridge Administrator}") String fullName
    ) {
        return args -> {
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                        "Admin bootstrap is enabled but ADMIN_BOOTSTRAP_EMAIL or ADMIN_BOOTSTRAP_PASSWORD is missing."
                );
            }
            boolean created = useCase.execute(email, password, fullName);
            if (created) {
                log.info("Initial production administrator was created from operator-provided credentials.");
            } else {
                log.info("Initial production administrator already exists; credentials were left unchanged.");
            }
        };
    }
}
