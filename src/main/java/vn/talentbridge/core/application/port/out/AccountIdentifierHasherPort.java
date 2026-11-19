package vn.talentbridge.core.application.port.out;

public interface AccountIdentifierHasherPort {
    String hash(String identifierType, String normalizedIdentifier);
}
