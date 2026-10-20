package vn.talentbridge.core.domain.exception;

public class InvalidPasswordResetTokenException
        extends DomainException {

    public InvalidPasswordResetTokenException() {
        super(
                40005,
                "Mã xác minh không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu mã mới."
        );
    }
}
