package vn.talentbridge.core.application.port.in;

public interface DeleteUserAccountUseCase {
    void deleteAccount(Long accountUserId, Long adminUserId, boolean blockIdentifiers);
}
