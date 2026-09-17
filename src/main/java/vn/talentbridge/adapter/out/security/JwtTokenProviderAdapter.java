package vn.talentbridge.adapter.out.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.TokenProviderPort;

import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    @Value("${talentbridge.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${talentbridge.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${talentbridge.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date toFutureExpiration(
            LocalDateTime expiresAt,
            Date issuedAt
    ) {
        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Token expiration must not be null"
            );
        }

        Date expiration = Date.from(
                expiresAt.atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        if (!expiration.after(issuedAt)) {
            throw new IllegalArgumentException(
                    "Token expiration must be in the future"
            );
        }

        return expiration;
    }

    @Override
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    @Override
    public String generateAccessToken(
            Long userId,
            String email,
            String role,
            String sessionId
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("sid", sessionId)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(
            Long userId,
            String email,
            String sessionId
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("sid", sessionId)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateAccessToken(
            Long userId,
            String email,
            String role,
            String sessionId,
            LocalDateTime expiresAt
    ) {
        Date now = new Date();
        Date expiryDate = toFutureExpiration(expiresAt, now);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("sid", sessionId)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(
            Long userId,
            String email,
            String sessionId,
            LocalDateTime expiresAt
    ) {
        Date now = new Date();
        Date expiryDate = toFutureExpiration(expiresAt, now);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("sid", sessionId)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }
    @Override
    public String getSessionIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("sid", String.class);
    }

    @Override
    public String getTokenTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("tokenType", String.class);
    }

    @Override
    public String hashRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be empty");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    @Override
    public boolean matchesRefreshTokenHash(
            String refreshToken,
            String storedHash
    ) {
        if (refreshToken == null
                || refreshToken.isBlank()
                || storedHash == null
                || !storedHash.matches("[0-9a-f]{64}")) {
            return false;
        }

        byte[] actualHash = hashRefreshToken(refreshToken)
                .getBytes(StandardCharsets.US_ASCII);

        byte[] expectedHash = storedHash
                .getBytes(StandardCharsets.US_ASCII);

        return MessageDigest.isEqual(actualHash, expectedHash);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}