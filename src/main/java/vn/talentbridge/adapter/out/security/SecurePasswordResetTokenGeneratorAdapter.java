package vn.talentbridge.adapter.out.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.PasswordResetTokenGeneratorPort;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class SecurePasswordResetTokenGeneratorAdapter
        implements PasswordResetTokenGeneratorPort {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec hashKey;

    public SecurePasswordResetTokenGeneratorAdapter(
            @Value("${talentbridge.password-reset.hash-key:${talentbridge.jwt.secret:}}")
            String hashingSecret
    ) {
        if (hashingSecret == null || hashingSecret.length() < 32) {
            throw new IllegalStateException(
                    "A password-reset hashing key of at least 32 characters is required"
            );
        }
        this.hashKey = new SecretKeySpec(
                hashingSecret.getBytes(StandardCharsets.UTF_8),
                HASH_ALGORITHM
        );
    }

    @Override
    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Override
    public String generateOtp() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(hashKey);
            byte[] hash = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Password-reset HMAC algorithm is unavailable",
                    exception
            );
        }
    }
}
