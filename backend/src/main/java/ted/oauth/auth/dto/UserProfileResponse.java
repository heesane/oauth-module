package ted.oauth.auth.dto;

import ted.oauth.user.Gender;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Gender gender,
        LocalDate birthday,
        String introduce,
        boolean profileCompleted
) {
}
