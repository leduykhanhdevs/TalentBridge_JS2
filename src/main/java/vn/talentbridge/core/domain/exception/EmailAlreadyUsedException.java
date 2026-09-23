package vn.talentbridge.core.domain.exception;

public class EmailAlreadyUsedException extends DomainException {
    public EmailAlreadyUsedException(String email) {
        super(40901, "Email này đã được sử dụng: " + email);
    }
}