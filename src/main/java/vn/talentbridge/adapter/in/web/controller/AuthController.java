package vn.talentbridge.adapter.in.web.controller;

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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "ÄÄƒng kÃ½, ÄÄƒng nháº­p, Refresh Token & Há»“ sÆ¡ cÃ¡ nhÃ¢n")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @PostMapping("/register")
    @Operation(summary = "ÄÄƒng kÃ½ tÃ i khoáº£n", description = "ÄÄƒng kÃ½ tÃ i khoáº£n má»›i cho á»¨ng viÃªn hoáº·c NhÃ  tuyá»ƒn dá»¥ng")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = registerUseCase.register(new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getPhone(),
                request.getRole()
        ));
        return new ResponseEntity<>(
                ApiResponse.created("ÄÄƒng kÃ½ tÃ i khoáº£n thÃ nh cÃ´ng", AuthResponse.from(result)),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "ÄÄƒng nháº­p", description = "XÃ¡c thá»±c tÃ i khoáº£n vÃ  tráº£ vá» JWT Access Token & Refresh Token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.login(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(ApiResponse.success("ÄÄƒng nháº­p thÃ nh cÃ´ng", AuthResponse.from(result)));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "LÃ m má»›i Access Token", description = "Cáº¥p Access Token má»›i tá»« Refresh Token há»£p lá»‡")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = refreshTokenUseCase.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("LÃ m má»›i token thÃ nh cÃ´ng", AuthResponse.from(result)));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Láº¥y thÃ´ng tin tÃ i khoáº£n hiá»‡n táº¡i", description = "YÃªu cáº§u JWT Bearer Token trong header Authorization")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResult result = getCurrentUserUseCase.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(result)));
    }
}