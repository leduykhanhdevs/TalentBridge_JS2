package vn.talentbridge.core.domain.exception;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super(40102, "Email hoặc mật khẩu không chính xác");
    }
}