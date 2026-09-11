package vn.talentbridge.core.domain.exception;

public class EmailAlreadyUsedException extends DomainException {
    public EmailAlreadyUsedException(String email) {
        super(40901, "Email nÃ y Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng: " + email);
    }
}