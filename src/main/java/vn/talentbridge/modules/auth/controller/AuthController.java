package vn.talentbridge.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.modules.auth.dto.request.LoginRequest;
import vn.talentbridge.modules.auth.dto.request.RefreshTokenRequest;
import vn.talentbridge.modules.auth.dto.request.RegisterRequest;
import vn.talentbridge.modules.auth.dto.response.AuthResponse;
import vn.talentbridge.modules.auth.dto.response.UserResponse;
import vn.talentbridge.modules.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, Đăng nhập, Refresh Token & Hồ sơ cá nhân")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Đăng ký tài khoản mới cho Ứng viên (ROLE_CANDIDATE) hoặc Nhà tuyển dụng (ROLE_RECRUITER)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created("Đăng ký tài khoản thành công", authResponse), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực tài khoản và trả về JWT Access Token & Refresh Token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authResponse));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới Access Token", description = "Cấp Access Token mới từ Refresh Token hợp lệ")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", authResponse));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Lấy thông tin tài khoản hiện tại", description = "Yêu cầu JWT Bearer Token trong header Authorization")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse userResponse = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}