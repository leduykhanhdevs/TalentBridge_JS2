package vn.talentbridge;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.talentbridge.adapter.out.security.JwtTokenProviderAdapter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

class JwtTokenProviderAdapterTest {

    private JwtTokenProviderAdapter tokenProvider;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProviderAdapter();

        // Khóa chỉ dùng cho test.
        signingKey = Keys.hmacShaKeyFor(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.US_ASCII)
        );

        ReflectionTestUtils.setField(
                tokenProvider,
                "jwtSecret",
                Base64.getEncoder().encodeToString(signingKey.getEncoded())
        );
        ReflectionTestUtils.setField(
                tokenProvider, "jwtExpirationMs", 3_600_000L
        );
        ReflectionTestUtils.setField(
                tokenProvider, "refreshExpirationMs", 604_800_000L
        );
    }

    @Test
    void refreshTokensHaveDifferentIds() {
        String first = tokenProvider.generateRefreshToken(
                1L, "test@example.com", "test-session"
        );
        String second = tokenProvider.generateRefreshToken(
                1L, "test@example.com", "test-session"
        );

        Claims firstClaims = parse(first);
        Claims secondClaims = parse(second);

        assertNotNull(firstClaims.getId());
        assertNotNull(secondClaims.getId());
        assertFalse(firstClaims.getId().isBlank());
        assertFalse(secondClaims.getId().isBlank());
        assertNotEquals(firstClaims.getId(), secondClaims.getId());
        assertNotEquals(first, second);

        assertEquals("test-session", secondClaims.get("sid", String.class));
        assertEquals("refresh", secondClaims.get("tokenType", String.class));
    }

    @Test
    void hashMatchesKnownSha256Value() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223"
                        + "b00361a396177a9cb410ff61f20015ad",
                tokenProvider.hashRefreshToken("abc")
        );
    }

    @Test
    void tokensWithSameLongPrefixRemainDifferent() {
        String prefix = "a".repeat(100);
        String oldToken = prefix + "-old";
        String newToken = prefix + "-new";

        String newHash = tokenProvider.hashRefreshToken(newToken);

        assertTrue(
                tokenProvider.matchesRefreshTokenHash(newToken, newHash)
        );
        assertFalse(
                tokenProvider.matchesRefreshTokenHash(oldToken, newHash)
        );
    }

    @Test
    void missingValuesAndLegacyBcryptHashesAreRejected() {
        String token = "sample-refresh-token";
        String hash = tokenProvider.hashRefreshToken(token);
        String legacyHash = new BCryptPasswordEncoder().encode(token);

        assertFalse(tokenProvider.matchesRefreshTokenHash(null, hash));
        assertFalse(tokenProvider.matchesRefreshTokenHash("", hash));
        assertFalse(tokenProvider.matchesRefreshTokenHash(token, null));
        assertFalse(tokenProvider.matchesRefreshTokenHash(token, legacyHash));
    }

    @Test
    void sessionTokensUseRequestedExpiration() {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(10)
                .withNano(0);

        String accessToken = tokenProvider.generateAccessToken(
                1L,
                "test@example.com",
                "ROLE_CANDIDATE",
                "test-session",
                expiresAt
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                1L,
                "test@example.com",
                "test-session",
                expiresAt
        );

        Date expectedExpiration = Date.from(
                expiresAt.atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        Claims accessClaims = parse(accessToken);
        Claims refreshClaims = parse(refreshToken);

        assertEquals(
                expectedExpiration,
                accessClaims.getExpiration()
        );
        assertEquals(
                expectedExpiration,
                refreshClaims.getExpiration()
        );

        assertEquals(
                "test-session",
                accessClaims.get("sid", String.class)
        );
        assertEquals(
                "access",
                accessClaims.get("tokenType", String.class)
        );

        assertEquals(
                "test-session",
                refreshClaims.get("sid", String.class)
        );
        assertEquals(
                "refresh",
                refreshClaims.get("tokenType", String.class)
        );
    }

    @Test
    void rejectsSessionTokenExpirationThatIsNotFuture() {
        LocalDateTime expiredAt =
                LocalDateTime.now().minusSeconds(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> tokenProvider.generateAccessToken(
                        1L,
                        "test@example.com",
                        "ROLE_CANDIDATE",
                        "test-session",
                        expiredAt
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tokenProvider.generateRefreshToken(
                        1L,
                        "test@example.com",
                        "test-session",
                        expiredAt
                )
        );
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}