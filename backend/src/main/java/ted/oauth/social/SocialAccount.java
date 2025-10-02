package ted.oauth.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ted.oauth.common.BaseTimeEntity;
import ted.oauth.user.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "social_account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
public class SocialAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 120)
    private String providerUserId;

    @Column(length = 120)
    private String email;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    private SocialAccount(AuthProvider provider, String providerUserId, String email, String displayName, User user) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.displayName = displayName;
        this.user = user;
    }

    public static SocialAccount create(AuthProvider provider, String providerUserId, String email, String displayName) {
        return new SocialAccount(provider, providerUserId, email, displayName, null);
    }

    public void associateUser(User user) {
        this.user = user;
    }


    public void updateProfile(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }
}
