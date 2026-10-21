package vn.talentbridge.core.application.port.in;

public interface LogoutUseCase {

    void logout(String accessToken, String refreshToken);

    default void logout(String accessToken) {
        logout(accessToken, null);
    }
}
