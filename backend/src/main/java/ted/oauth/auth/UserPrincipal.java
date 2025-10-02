package ted.oauth.auth;

import ted.oauth.user.User;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean profileCompleted;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes = new HashMap<>();

    private UserPrincipal(Long id,
                          String email,
                          String password,
                          boolean profileCompleted,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.profileCompleted = profileCompleted;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.isProfileCompleted(), authorities);
    }

    public static UserPrincipal fromSocial(String email, boolean profileCompleted) {
        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(null, email, "", profileCompleted, authorities);
    }

    public UserPrincipal withAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Long getId() {
        return id;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
