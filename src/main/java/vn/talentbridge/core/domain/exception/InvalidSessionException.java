package vn.talentbridge.core.domain.exception;

public class InvalidSessionException extends DomainException {

    public InvalidSessionException() {
        super(40101, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
    }
}