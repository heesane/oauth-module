package ted.oauth.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.oauth.user.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByUser(User user);
}
