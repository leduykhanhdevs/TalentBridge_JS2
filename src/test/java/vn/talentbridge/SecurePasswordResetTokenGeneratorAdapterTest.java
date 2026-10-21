package vn.talentbridge;

import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.security.SecurePasswordResetTokenGeneratorAdapter;

import static org.junit.jupiter.api.Assertions.*;

class SecurePasswordResetTokenGeneratorAdapterTest {

    private static final String SECRET = "local-test-reset-key-at-least-32-chars";

    @Test
    void generatesSixDigitOtpIncludingLeadingZeroes() {
        SecurePasswordResetTokenGeneratorAdapter generator =
                new SecurePasswordResetTokenGeneratorAdapter(SECRET);

        assertTrue(generator.generateOtp().matches("[0-9]{6}"));
    }

    @Test
    void hashesDeterministicallyWithServerSideHmacKey() {
        SecurePasswordResetTokenGeneratorAdapter generator =
                new SecurePasswordResetTokenGeneratorAdapter(SECRET);

        String first = generator.hashToken("password-reset-otp:1:2:123456");

        assertEquals(first, generator.hashToken("password-reset-otp:1:2:123456"));
        assertTrue(first.matches("[0-9a-f]{64}"));
        assertNotEquals(
                first,
                new SecurePasswordResetTokenGeneratorAdapter(
                        "another-server-reset-key-at-least-32-chars"
                ).hashToken("password-reset-otp:1:2:123456")
        );
    }

    @Test
    void rejectsShortHmacKey() {
        assertThrows(
                IllegalStateException.class,
                () -> new SecurePasswordResetTokenGeneratorAdapter("short-key")
        );
    }
}
