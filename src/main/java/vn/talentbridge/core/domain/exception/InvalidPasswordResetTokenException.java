package vn.talentbridge.core.domain.exception;

public class InvalidPasswordResetTokenException
        extends DomainException {

    public InvalidPasswordResetTokenException() {
        super(
                40005,
                "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"
        );
    }
}