package vn.talentbridge.core.application.port.out;

public interface TokenProviderPort {
    String generateAccessToken(Long userId, String email, String role);
    String generateRefreshToken(Long userId, String email);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    Long getUserIdFromToken(String token);
    String getRoleFromToken(String token);
    String generateAccessToken(
            Long userId,
            String email,
            String role,
            String sessionId
    );

    String generateRefreshToken(
            Long userId,
            String email,
            String sessionId
    );

    String getSessionIdFromToken(String token);

    String getTokenTypeFromToken(String token);

    String hashRefreshToken(String refreshToken);

    boolean matchesRefreshTokenHash(String refreshToken, String storedHash);
}