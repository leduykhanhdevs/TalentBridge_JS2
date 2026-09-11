package vn.talentbridge.core.application.port.out;

public interface TokenProviderPort {
    String generateAccessToken(Long userId, String email, String role);
    String generateRefreshToken(Long userId, String email);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    Long getUserIdFromToken(String token);
}