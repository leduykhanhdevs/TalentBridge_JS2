package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.adapter.in.web.dto.request.LoginRequest;
import vn.talentbridge.adapter.in.web.dto.request.RefreshTokenRequest;
import vn.talentbridge.adapter.in.web.dto.request.RegisterRequest;
import vn.talentbridge.adapter.in.web.dto.response.AuthResponse;
import vn.talentbridge.adapter.in.web.dto.response.UserResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.GetCurrentUserUseCase;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.application.port.in.RefreshTokenUseCase;
import vn.talentbridge.core.application.port.in.RegisterUseCase;
import vn.talentbridge.core.application.port.in.LogoutUseCase;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đặc tả API Xác thực, Phân quyền, Quản lý Token & Session (HRPM-24)")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Đăng ký tài khoản cho Ứng viên (ROLE_CANDIDATE) hoặc Nhà tuyển dụng (ROLE_RECRUITER)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký tài khoản thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu gửi lên không hợp lệ (Validation Error)",
                    content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email đã tồn tại trên hệ thống (Conflict)",
                    content = @Content(mediaType = "application/json"))
    })
    @Transactional(rollbackFor = Throwable.class)
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = registerUseCase.register(new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getPhone(),
                request.getRole(),
                request.getPosition()
        ));
        return new ResponseEntity<>(
                ApiResponse.created("Đăng ký tài khoản thành công", AuthResponse.from(result)),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực email & mật khẩu, trả về cặp JWT Access Token (24h) và Refresh Token (7 ngày)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công, trả về Access & Refresh Token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email hoặc mật khẩu để trống hoặc sai định dạng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không chính xác (Bad Credentials)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Tài khoản đang bị khóa (BANNED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Đăng nhập sai quá số lần cho phép")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.login(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", AuthResponse.from(result)));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới Access Token", description = "Cấp Access Token mới khi Access Token cũ đã hết hạn bằng Refresh Token hợp lệ")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cấp Access Token mới thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh Token không được để trống"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token không hợp lệ hoặc đã hết hạn")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = refreshTokenUseCase.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", AuthResponse.from(result)));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Đăng xuất",
            description = "Thu hồi phiên đăng nhập tương ứng với Access Token hiện tại"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng xuất thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token hoặc phiên đăng nhập không hợp lệ"
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {
        String accessToken = authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : null;

        logoutUseCase.logout(accessToken);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Đăng xuất thành công",
                        null
                )
        );
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Lấy thông tin tài khoản hiện tại", description = "Trích xuất thông tin người dùng từ JWT Token trong header Authorization: Bearer <token>")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin tài khoản thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực hoặc JWT Token không hợp lệ/hết hạn"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResult result = getCurrentUserUseCase.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(result)));
    }
}
