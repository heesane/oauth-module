package ted.oauth.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.oauth.auth.dto.LoginRequest;
import ted.oauth.auth.dto.RegisterRequest;
import ted.oauth.auth.dto.TokenRefreshRequest;
import ted.oauth.auth.dto.TokenResponse;
import ted.oauth.user.User;
import ted.oauth.user.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        return jwtTokenService.issueTokens(user);
    }

    public User register(RegisterRequest request) {
        validateDuplicate(request.email(), request.nickname());
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
                request.email(),
                request.name(),
                request.nickname(),
                encodedPassword,
                request.gender(),
                request.birthday(),
                request.introduce(),
                true
        );
        return userRepository.save(user);
    }

    public TokenResponse refresh(TokenRefreshRequest request) {
        return jwtTokenService.refreshTokens(request.refreshToken());
    }

    public void logout(TokenRefreshRequest request) {
        jwtTokenService.revokeRefreshToken(request.refreshToken());
        SecurityContextHolder.clearContext();
    }

    private void validateDuplicate(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }
}
