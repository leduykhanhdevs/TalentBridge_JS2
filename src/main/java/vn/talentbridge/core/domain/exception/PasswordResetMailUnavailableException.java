package vn.talentbridge.core.domain.exception;

public class PasswordResetMailUnavailableException extends DomainException {

    public PasswordResetMailUnavailableException() {
        super(
                50301,
                "Dịch vụ gửi email khôi phục mật khẩu hiện không khả dụng. "
                        + "Vui lòng thử lại sau hoặc liên hệ quản trị viên."
        );
    }
}
