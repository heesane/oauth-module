package ted.oauth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ted.oauth.common.BaseTimeEntity;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, unique = true, length = 60)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String introduce;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted = true;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    private User(String email,
                 String name,
                 String nickname,
                 String password,
                 Gender gender,
                 LocalDate birthday,
                 String introduce,
                 boolean profileCompleted) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.gender = gender;
        this.birthday = birthday;
        this.introduce = introduce;
        this.profileCompleted = profileCompleted;
    }

    public static User create(String email,
                              String name,
                              String nickname,
                              String password,
                              Gender gender,
                              LocalDate birthday,
                              String introduce,
                              boolean profileCompleted) {
        return new User(email, name, nickname, password, gender, birthday, introduce, profileCompleted);
    }

    public void completeProfile(String name,
                                String nickname,
                                Gender gender,
                                LocalDate birthday,
                                String introduce) {
        this.name = name;
        this.nickname = nickname;
        this.gender = gender;
        this.birthday = birthday;
        this.introduce = introduce;
        this.profileCompleted = true;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateIntroduce(String introduce) {
        this.introduce = introduce;
        this.profileCompleted = true;
    }
}
