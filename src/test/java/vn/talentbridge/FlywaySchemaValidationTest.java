package vn.talentbridge;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.flyway.baseline-on-migrate=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywaySchemaValidationTest {

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    @DisplayName("Flyway baseline migration should execute and Hibernate schema validation should pass")
    void testFlywayBaselineMigrationAndSchemaValidation() {
        assertThat(flyway).isNotNull();
        MigrationInfo current = flyway.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getVersion().getVersion()).isEqualTo("1");
        assertThat(current.getDescription()).isIn("baseline", "<< Flyway Baseline >>");
    }
}
