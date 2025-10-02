package ted.oauth.auth.dto;

import ted.oauth.user.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String nickname,
        @NotBlank String password,
        @NotNull Gender gender,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthday,
        @NotBlank String introduce
) {
}
