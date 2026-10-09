package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import vn.talentbridge.config.AdminBootstrapConfiguration;
import vn.talentbridge.core.application.port.in.BootstrapAdminAccountUseCase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminBootstrapConfigurationTest {
    private static final String PASSWORD = "Strong-production-passphrase-2026";
    private final BootstrapAdminAccountUseCase useCase = mock(BootstrapAdminAccountUseCase.class);

    private ApplicationContextRunner productionContext() {
        return new ApplicationContextRunner()
                .withUserConfiguration(AdminBootstrapConfiguration.class)
                .withBean(BootstrapAdminAccountUseCase.class, () -> useCase)
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod"));
    }

    @Test
    void bootstrapRunnerIsDisabledByDefault() {
        productionContext().run(context -> assertFalse(context.containsBean("bootstrapAdminAccount")));
    }

    @Test
    void bootstrapRunnerOnlyExistsInProductionProfile() {
        new ApplicationContextRunner()
                .withUserConfiguration(AdminBootstrapConfiguration.class)
                .withBean(BootstrapAdminAccountUseCase.class, () -> useCase)
                .withPropertyValues("talentbridge.admin-bootstrap.enabled=true")
                .run(context -> assertFalse(context.containsBean("bootstrapAdminAccount")));
    }

    @Test
    void enabledRunnerUsesOperatorCredentialsWithoutLoggingOrChangingThem() {
        when(useCase.execute("admin@example.com", PASSWORD, "Production Admin")).thenReturn(true);

        productionContext()
                .withPropertyValues(
                        "talentbridge.admin-bootstrap.enabled=true",
                        "talentbridge.admin-bootstrap.email=admin@example.com",
                        "talentbridge.admin-bootstrap.password=" + PASSWORD,
                        "talentbridge.admin-bootstrap.full-name=Production Admin"
                )
                .run(context -> {
                    assertTrue(context.containsBean("bootstrapAdminAccount"));
                    ApplicationRunner runner = context.getBean("bootstrapAdminAccount", ApplicationRunner.class);
                    assertDoesNotThrow(() -> runner.run(new DefaultApplicationArguments(new String[0])));
                    verify(useCase).execute("admin@example.com", PASSWORD, "Production Admin");
                });
    }

    @Test
    void enabledRunnerFailsFastWhenCredentialsAreMissing() {
        productionContext()
                .withPropertyValues("talentbridge.admin-bootstrap.enabled=true")
                .run(context -> {
                    ApplicationRunner runner = context.getBean("bootstrapAdminAccount", ApplicationRunner.class);
                    assertThrows(
                            IllegalStateException.class,
                            () -> runner.run(new DefaultApplicationArguments(new String[0]))
                    );
                    verifyNoInteractions(useCase);
                });
    }
}
