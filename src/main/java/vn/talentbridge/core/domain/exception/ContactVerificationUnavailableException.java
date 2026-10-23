package vn.talentbridge.core.domain.exception;

public class ContactVerificationUnavailableException extends RuntimeException {

    public ContactVerificationUnavailableException(String message) {
        super(message);
    }

    public ContactVerificationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
