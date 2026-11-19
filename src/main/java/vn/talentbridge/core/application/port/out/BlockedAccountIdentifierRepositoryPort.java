package vn.talentbridge.core.application.port.out;

public interface BlockedAccountIdentifierRepositoryPort {
    boolean exists(String identifierType, String identifierHash);

    void save(String identifierType, String identifierHash);
}
