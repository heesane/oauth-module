package ted.oauth.auth;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.oauth.auth.dto.TokenResponse;
import ted.oauth.user.User;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds());

        refreshTokenRepository.deleteAllByUser(user);
        refreshTokenRepository.saveAndFlush(RefreshToken.create(user, refreshToken, expiresAt));

        return new TokenResponse(accessToken, refreshToken, "Bearer", jwtTokenProvider.getAccessTokenValiditySeconds());
    }

    public TokenResponse refreshTokens(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);

        return issueTokens(user);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
