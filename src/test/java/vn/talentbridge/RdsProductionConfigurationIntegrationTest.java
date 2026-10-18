package vn.talentbridge;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.config.location=file:src/main/resources/application.yml",
        "spring.datasource.url=jdbc:h2:mem:rdsProfile;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "SPRING_DATASOURCE_URL=jdbc:h2:mem:rdsProfile;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "SPRING_DATASOURCE_USERNAME=sa",
        "SPRING_DATASOURCE_PASSWORD=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.url=jdbc:h2:mem:rdsProfile;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "TALENTBRIDGE_JWT_SECRET=RdsProfileTestSecretKeyAtLeastThirtyTwoCharactersLong"
})
@ActiveProfiles("prod")
class RdsProductionConfigurationIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private Flyway flyway;

    @Test
    void productionProfileRunsKnownMigrationsAndValidatesTheSchemaWithoutBaselining() {
        assertThat(environment.getProperty("talentbridge.jwt.secret"))
                .isEqualTo("RdsProfileTestSecretKeyAtLeastThirtyTwoCharactersLong");
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class)).isFalse();
        assertThat(environment.getProperty("spring.flyway.user")).isEqualTo("sa");
        assertThat(flyway.info().applied()).hasSize(3);
    }
}
