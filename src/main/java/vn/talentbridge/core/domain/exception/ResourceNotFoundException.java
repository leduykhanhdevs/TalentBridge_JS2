package vn.talentbridge.core.domain.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resourceName, Object id) {
        super(40401, resourceName + " không tồn tại với ID: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(40401, message);
    }
}