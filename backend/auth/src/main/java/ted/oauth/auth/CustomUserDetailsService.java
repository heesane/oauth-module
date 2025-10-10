package ted.oauth.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.oauth.user.User;
import ted.oauth.user.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = identifier.contains("@")
                ? userRepository.findByEmail(identifier).orElseThrow(() ->
                        new UsernameNotFoundException("이메일을 찾을 수 없습니다."))
                : userRepository.findByNickname(identifier).orElseThrow(() ->
                        new UsernameNotFoundException("닉네임을 찾을 수 없습니다."));

        return UserPrincipal.from(user);
    }
}
