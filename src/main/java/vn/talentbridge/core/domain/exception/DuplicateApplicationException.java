package vn.talentbridge.core.domain.exception;

public class DuplicateApplicationException extends DomainException {
    public DuplicateApplicationException() {
        super(40902, "Bạn đã nộp hồ sơ vào vị trí này rồi");
    }
}
