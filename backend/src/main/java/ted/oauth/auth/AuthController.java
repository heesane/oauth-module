package ted.oauth.auth;

import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ted.oauth.auth.dto.ApiResponse;
import ted.oauth.auth.dto.LoginRequest;
import ted.oauth.auth.dto.RegisterRequest;
import ted.oauth.auth.dto.TokenRefreshRequest;
import ted.oauth.auth.dto.TokenResponse;
import ted.oauth.auth.dto.UserProfileResponse;
import ted.oauth.user.User;
import ted.oauth.user.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다.", user.getId()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse tokens = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃되었습니다.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            Optional<User> userOptional = Optional.ofNullable(principal.getId())
                    .flatMap(userRepository::findById);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserProfileResponse response = new UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getNickname(),
                        user.getGender(),
                        user.getBirthday(),
                        user.getIntroduce(),
                        user.isProfileCompleted()
                );
                return ResponseEntity.ok(ApiResponse.ok(response));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("로그인된 사용자가 없습니다."));
    }
}
