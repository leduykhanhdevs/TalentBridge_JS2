package vn.talentbridge.core.domain.exception;

public class TooManyLoginAttemptsException extends DomainException {
    public TooManyLoginAttemptsException() {
        super(42901, "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau");
    }
}
