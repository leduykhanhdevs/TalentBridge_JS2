package vn.talentbridge.core.domain.exception;

public class UserAccountLockedException extends DomainException {
    public UserAccountLockedException() {
        super(40301, "Tài khoản của bạn đã bị khóa");
    }

    public UserAccountLockedException(String message) {
        super(40301, message);
    }
}