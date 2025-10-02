package ted.oauth.auth;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.oauth.social.AuthProvider;
import ted.oauth.user.User;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserProvisioningService socialProvisioningService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("[OAuth2] loadUser registrationId={}", userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oAuth2User = super.loadUser(userRequest);

        AuthProvider provider = AuthProvider.valueOf(userRequest.getClientRegistration()
                .getRegistrationId()
                .toUpperCase(Locale.ROOT));

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerUserId = extractProviderUserId(provider, attributes);
        String email = extractEmail(provider, attributes);
        String name = extractDisplayName(provider, attributes);

        User user = socialProvisioningService.ensureUser(provider, providerUserId, email, name);
        log.info("[OAuth2] user ensured userId={} email={}", user.getId(), user.getEmail());

        return UserPrincipal.from(user).withAttributes(attributes);
    }

    private String extractProviderUserId(AuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case KAKAO -> String.valueOf(attributes.get("id"));
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield response != null ? (String) response.get("id") : null;
            }
            case APPLE -> (String) attributes.get("sub");
        };
    }

    private String extractEmail(AuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("email");
            case KAKAO -> {
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                yield account != null ? (String) account.get("email") : null;
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield response != null ? (String) response.get("email") : null;
            }
            case APPLE -> (String) attributes.get("email");
        };
    }

    private String extractDisplayName(AuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("name");
            case KAKAO -> {
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                if (account == null) {
                    yield null;
                }
                Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                yield profile != null ? (String) profile.get("nickname") : null;
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield response != null ? (String) response.get("name") : null;
            }
            case APPLE -> {
                Object nameAttr = attributes.get("name");
                if (nameAttr instanceof String name) {
                    yield name;
                }
                if (nameAttr instanceof Map<?, ?> nameMap) {
                    Object first = nameMap.get("firstName");
                    Object last = nameMap.get("lastName");
                    String combined = ((first != null ? first.toString() : "") + " "
                            + (last != null ? last.toString() : "")).trim();
                    yield combined.isEmpty() ? null : combined;
                }
                yield null;
            }
        };
    }
}
