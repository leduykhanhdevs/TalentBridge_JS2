package vn.talentbridge.adapter.in.web.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(40001, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(40002, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_INPUT_FORMAT(40003, "Định dạng dữ liệu không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(40004, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(40005, "Vai trò không hợp lệ", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED(40101, "Báº¡n chÆ°a Ä‘Äƒng nháº­p hoáº·c phiÃªn lÃ m viá»‡c Ä‘Ã£ háº¿t háº¡n", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(40102, "Email hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(40103, "Token xÃ¡c thá»±c khÃ´ng há»£p lá»‡", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(40104, "Token xÃ¡c thá»±c Ä‘Ã£ háº¿t háº¡n", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN(40301, "Báº¡n khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n thao tÃ¡c nÃ y", HttpStatus.FORBIDDEN),
    ACCOUNT_BANNED(40302, "TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ bá»‹ táº¡m khÃ³a bá»Ÿi Quáº£n trá»‹ viÃªn", HttpStatus.FORBIDDEN),

    // 404 Not Found
    USER_NOT_FOUND(40401, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng", HttpStatus.NOT_FOUND),
    JOB_NOT_FOUND(40402, "KhÃ´ng tÃ¬m tháº¥y tin tuyá»ƒn dá»¥ng", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND(40403, "KhÃ´ng tÃ¬m tháº¥y doanh nghiá»‡p", HttpStatus.NOT_FOUND),
    CANDIDATE_NOT_FOUND(40404, "KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ á»©ng viÃªn", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_FOUND(40405, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n á»©ng tuyá»ƒn", HttpStatus.NOT_FOUND),
    RESUME_NOT_FOUND(40406, "KhÃ´ng tÃ¬m tháº¥y CV", HttpStatus.NOT_FOUND),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(40901, "Email này đã được sử dụng", HttpStatus.CONFLICT),
    APPLICATION_ALREADY_EXISTS(40902, "Bạn đã nộp hồ sơ vào vị trí này rồi", HttpStatus.CONFLICT),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(42901, "Quá nhiều yêu cầu. Vui lòng thử lại sau.", HttpStatus.TOO_MANY_REQUESTS),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(50001, "Lỗi máy chủ nội bộ. Vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}