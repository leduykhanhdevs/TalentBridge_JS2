package vn.talentbridge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import vn.talentbridge.core.application.port.in.AdminBootstrapResult;
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
            @Value("${talentbridge.admin-bootstrap.full-name:TalentBridge Administrator}") String fullName,
            @Value("${talentbridge.admin-bootstrap.promote-existing:false}") boolean promoteExistingAccount
    ) {
        return args -> {
            if (email.isBlank()) {
                throw new IllegalStateException(
                        "Admin bootstrap is enabled but ADMIN_BOOTSTRAP_EMAIL is missing."
                );
            }
            AdminBootstrapResult result = useCase.execute(email, password, fullName, promoteExistingAccount);
            switch (result) {
                case CREATED -> log.info("Initial production administrator was created from operator-provided credentials.");
                case PROMOTED_EXISTING -> log.info("Existing active production account was granted ROLE_ADMIN by explicit operator opt-in.");
                case ALREADY_ADMIN -> log.info("Initial production administrator already exists; credentials were left unchanged.");
            }
        };
    }
}
