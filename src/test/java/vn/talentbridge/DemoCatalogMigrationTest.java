package vn.talentbridge;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoCatalogMigrationTest {

    @Test
    void seedsTwentyClearlyMarkedCompaniesAndOneHundredNonActionableJobs() throws Exception {
        String databaseName = "demo_catalog_" + UUID.randomUUID().toString().replace("-", "");
        String jdbcUrl = "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";

        Flyway.configure()
                .dataSource(jdbcUrl, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            assertCount(statement, "SELECT COUNT(*) FROM companies WHERE is_demo = TRUE", 20);
            assertCount(statement, "SELECT COUNT(*) FROM companies WHERE is_demo = TRUE AND status = 'PENDING'", 20);
            assertCount(statement, "SELECT COUNT(*) FROM jobs WHERE is_demo = TRUE", 100);
            assertCount(statement, "SELECT COUNT(*) FROM jobs WHERE is_demo = TRUE AND status = 'ACTIVE'", 100);
            assertCount(statement, "SELECT COUNT(*) FROM jobs WHERE is_demo = TRUE AND recruiter_user_id IS NULL", 100);
            assertCount(statement, "SELECT COUNT(*) FROM applications a JOIN jobs j ON j.id = a.job_id WHERE j.is_demo = TRUE", 0);
            assertCount(statement, "SELECT COUNT(*) FROM job_skills js JOIN jobs j ON j.id = js.job_id WHERE j.is_demo = TRUE", 280);
        }
    }

    private static void assertCount(Statement statement, String sql, long expected) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            assertEquals(expected, resultSet.getLong(1), sql);
        }
    }
}
