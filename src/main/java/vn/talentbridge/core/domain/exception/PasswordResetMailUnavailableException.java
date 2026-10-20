package vn.talentbridge.core.domain.exception;

public class PasswordResetMailUnavailableException extends DomainException {

    public PasswordResetMailUnavailableException() {
        super(
                50301,
                "Dịch vụ gửi email khôi phục mật khẩu chưa được cấu hình. "
                        + "Vui lòng liên hệ quản trị viên."
        );
    }
}
