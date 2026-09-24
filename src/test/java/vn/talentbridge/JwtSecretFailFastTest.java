package vn.talentbridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import vn.talentbridge.adapter.out.security.JwtTokenProviderAdapter;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSecretFailFastTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtTokenProviderAdapter.class);

    @Test
    @DisplayName("Startup fail-fast: Context fails when talentbridge.jwt.secret is completely missing")
    void contextFailsWhenJwtSecretMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure().getCause())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("JWT Secret must be configured via environment variable JWT_SECRET and cannot be blank");
        });
    }

    @Test
    @DisplayName("Startup fail-fast: Context fails when talentbridge.jwt.secret is blank")
    void contextFailsWhenJwtSecretIsBlank() {
        contextRunner
                .withPropertyValues("talentbridge.jwt.secret=   ")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure().getCause())
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("JWT Secret must be configured via environment variable JWT_SECRET and cannot be blank");
                });
    }

    @Test
    @DisplayName("Startup success: Context starts when valid test secret is provided")
    void contextStartsWhenValidSecretProvided() {
        contextRunner
                .withPropertyValues("talentbridge.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10YWxlbnRicmlkZ2UtdGVzdGluZy0xMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(JwtTokenProviderAdapter.class);
                });
    }
}
