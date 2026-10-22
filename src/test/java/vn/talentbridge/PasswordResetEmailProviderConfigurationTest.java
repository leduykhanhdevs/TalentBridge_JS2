package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;
import vn.talentbridge.adapter.out.email.GoogleAppsScriptPasswordResetEmailAdapter;
import vn.talentbridge.adapter.out.email.JavaMailPasswordResetEmailAdapter;
import vn.talentbridge.core.application.port.out.PasswordResetEmailPort;
import vn.talentbridge.core.domain.exception.PasswordResetMailUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PasswordResetEmailProviderConfigurationTest {

    private static final String VALID_APPS_SCRIPT_URL =
            "https://script.google.com/macros/s/test_deployment_123/exec";

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            JavaMailPasswordResetEmailAdapter.class,
                            GoogleAppsScriptPasswordResetEmailAdapter.class
                    )
                    .withBean(ObjectMapper.class, ObjectMapper::new)
                    .withBean(JavaMailSender.class, () -> mock(JavaMailSender.class));

    @Test
    void defaultsToSmtpProviderForBackwardCompatibility() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PasswordResetEmailPort.class);
            assertThat(context)
                    .hasSingleBean(JavaMailPasswordResetEmailAdapter.class)
                    .doesNotHaveBean(GoogleAppsScriptPasswordResetEmailAdapter.class);
        });
    }

    @Test
    void selectsAppsScriptProviderWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "talentbridge.password-reset.mail-provider=apps-script",
                        "talentbridge.password-reset.apps-script-url="
                                + VALID_APPS_SCRIPT_URL,
                        "talentbridge.password-reset.apps-script-secret=test-secret",
                        "talentbridge.password-reset.mail-enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordResetEmailPort.class);
                    assertThat(context)
                            .hasSingleBean(GoogleAppsScriptPasswordResetEmailAdapter.class)
                            .doesNotHaveBean(JavaMailPasswordResetEmailAdapter.class);
                });
    }

    @Test
    void failsFastWhenEnabledAppsScriptProviderHasNoCredentials() {
        contextRunner
                .withPropertyValues(
                        "talentbridge.password-reset.mail-provider=apps-script",
                        "talentbridge.password-reset.mail-enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertRootCauseContains(
                            context.getStartupFailure(),
                            "shared secret are required"
                    );
                });
    }

    @Test
    void rejectsNonGoogleOrNonHttpsRelayUrls() {
        contextRunner
                .withPropertyValues(
                        "talentbridge.password-reset.mail-provider=apps-script",
                        "talentbridge.password-reset.apps-script-url=https://example.com/relay",
                        "talentbridge.password-reset.apps-script-secret=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertRootCauseContains(
                            context.getStartupFailure(),
                            "deployed HTTPS /exec URL"
                    );
                });
    }

    @Test
    void refusesToSendWhenMailIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "talentbridge.password-reset.mail-provider=apps-script"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PasswordResetEmailPort mailPort = context.getBean(
                            PasswordResetEmailPort.class
                    );
                    assertThatThrownBy(() -> mailPort.sendPasswordResetOtp(
                            "candidate@example.com",
                            "123456",
                            java.time.LocalDateTime.now().plusMinutes(15)
                    )).isInstanceOf(PasswordResetMailUnavailableException.class);
                });
    }

    private void assertRootCauseContains(Throwable failure, String message) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        assertThat(rootCause).hasMessageContaining(message);
    }
}
