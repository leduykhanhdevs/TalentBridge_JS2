package vn.talentbridge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(40001, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(40002, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_INPUT_FORMAT(40003, "Định dạng dữ liệu không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(40004, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED(40101, "Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(40102, "Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(40103, "Token xác thực không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(40104, "Token xác thực đã hết hạn", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN(40301, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    ACCOUNT_BANNED(40302, "Tài khoản của bạn đã bị tạm khóa bởi Quản trị viên", HttpStatus.FORBIDDEN),

    // 404 Not Found
    USER_NOT_FOUND(40401, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    JOB_NOT_FOUND(40402, "Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND(40403, "Không tìm thấy doanh nghiệp", HttpStatus.NOT_FOUND),
    CANDIDATE_NOT_FOUND(40404, "Không tìm thấy hồ sơ ứng viên", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_FOUND(40405, "Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND),
    RESUME_NOT_FOUND(40406, "Không tìm thấy CV", HttpStatus.NOT_FOUND),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(40901, "Email này đã được sử dụng", HttpStatus.CONFLICT),
    APPLICATION_ALREADY_EXISTS(40902, "Bạn đã nộp hồ sơ vào vị trí này rồi", HttpStatus.CONFLICT),

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
