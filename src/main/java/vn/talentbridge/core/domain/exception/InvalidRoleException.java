package vn.talentbridge.core.domain.exception;

public class InvalidRoleException extends DomainException {
    public InvalidRoleException(String role) {
        super(40005, "Vai trò không hợp lệ: '" + (role == null ? "null" : role) + "'. Hệ thống chỉ chấp nhận ROLE_CANDIDATE hoặc ROLE_RECRUITER");
    }
}
