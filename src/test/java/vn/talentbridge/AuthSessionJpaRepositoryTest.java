package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import vn.talentbridge.adapter.out.persistence.entity.AuthSessionJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.AuthSessionJpaRepository;
import vn.talentbridge.config.JpaAuditingConfig;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class AuthSessionJpaRepositoryTest {

    @Autowired
    private AuthSessionJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private LocalDateTime now;
    private UserJpaEntity user;
    private AuthSessionJpaEntity session;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().withNano(0);

        user = new UserJpaEntity();
        user.setEmail("session-test@example.com");
        user.setPasswordHash("unused-test-password-hash");
        user.setFullName("Session Test");
        user.setStatus(UserStatus.ACTIVE);
        entityManager.persistAndFlush(user);

        session = new AuthSessionJpaEntity();
        session.setSessionId("test-session");
        session.setUser(user);
        session.setRefreshTokenHash("old-hash");
        session.setExpiresAt(now.plusDays(1));
        entityManager.persistAndFlush(session);
    }

    @Test
    void rotatesActiveSessionWithoutExtendingExpiry() {
        int updated = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId(),
                "old-hash",
                "new-hash",
                now
        );

        AuthSessionJpaEntity stored = loadSession();

        assertEquals(1, updated);
        assertEquals("new-hash", stored.getRefreshTokenHash());
        assertEquals(now.plusDays(1), stored.getExpiresAt());
        assertNull(stored.getRevokedAt());
    }

    @Test
    void oldHashCannotBeUsedTwice() {
        int first = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId(),
                "old-hash",
                "first-new-hash",
                now
        );

        int second = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId(),
                "old-hash",
                "second-new-hash",
                now
        );

        assertEquals(1, first);
        assertEquals(0, second);
        assertEquals(
                "first-new-hash",
                loadSession().getRefreshTokenHash()
        );
    }

    @Test
    void revokedSessionCannotBeRotatedOrReactivated() {
        LocalDateTime revokedAt = now.minusMinutes(1);
        session.setRevokedAt(revokedAt);
        entityManager.flush();

        int updated = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId(),
                "old-hash",
                "new-hash",
                now
        );

        AuthSessionJpaEntity stored = loadSession();

        assertEquals(0, updated);
        assertEquals("old-hash", stored.getRefreshTokenHash());
        assertEquals(revokedAt, stored.getRevokedAt());
    }

    @Test
    void sessionExpiringExactlyNowCannotBeRotated() {
        session.setExpiresAt(now);
        entityManager.flush();

        int updated = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId(),
                "old-hash",
                "new-hash",
                now
        );

        assertEquals(0, updated);
        assertEquals("old-hash", loadSession().getRefreshTokenHash());
    }

    @Test
    void differentUserCannotRotateSession() {
        int updated = repository.rotateRefreshTokenIfActive(
                "test-session",
                user.getId() + 1,
                "old-hash",
                "new-hash",
                now
        );

        assertEquals(0, updated);
        assertEquals("old-hash", loadSession().getRefreshTokenHash());
    }

    private AuthSessionJpaEntity loadSession() {
        return repository.findBySessionId("test-session")
                .orElseThrow();
    }
}