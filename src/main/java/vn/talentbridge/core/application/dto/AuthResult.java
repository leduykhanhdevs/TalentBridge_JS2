package vn.talentbridge.core.application.dto;

public record AuthResult(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserResult user
) {
    public static AuthResult of(String accessToken, String refreshToken, long expiresIn, UserResult user) {
        return new AuthResult(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}