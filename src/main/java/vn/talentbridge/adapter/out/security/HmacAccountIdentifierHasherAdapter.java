package vn.talentbridge.adapter.out.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.AccountIdentifierHasherPort;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class HmacAccountIdentifierHasherAdapter implements AccountIdentifierHasherPort {
    private static final String ALGORITHM = "HmacSHA256";
    private final byte[] secret;

    public HmacAccountIdentifierHasherAdapter(
            @Value("${talentbridge.account-blocking.hmac-secret:${talentbridge.jwt.secret:}}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Account blocking requires a stable HMAC secret");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String hash(String identifierType, String normalizedIdentifier) {
        if (identifierType == null || normalizedIdentifier == null || normalizedIdentifier.isBlank()) {
            throw new IllegalArgumentException("Identifier type and value are required");
        }

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            byte[] digest = mac.doFinal((identifierType.toUpperCase(Locale.ROOT) + ":" + normalizedIdentifier)
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not hash account identifier", exception);
        }
    }
}
