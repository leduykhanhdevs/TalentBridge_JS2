package vn.talentbridge.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Standard CEO-grade REST API Response envelope for all endpoints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cấu trúc phản hồi chuẩn RESTful (ApiResponse Envelope)")
public class ApiResponse<T> {

    @Schema(description = "HTTP Status Code", example = "200")
    private int statusCode;

    @Schema(description = "Thông báo kết quả thao tác", example = "Thao tác thành công")
    private String message;

    @Schema(description = "Dữ liệu phản hồi chính")
    private T data;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời điểm trả về kết quả", example = "2026-09-14 10:00:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message("Thao tác thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
