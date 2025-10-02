package ted.oauth.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ted.oauth.social.AuthProvider;
import ted.oauth.auth.dto.TokenResponse;
import ted.oauth.user.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final SocialUserProvisioningService socialProvisioningService;
  private final JwtTokenService jwtTokenService;

  @Value("${FRONTEND_BASE_URL:http://localhost:3000}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (authentication instanceof OAuth2AuthenticationToken token) {
      log.info("[OAuth2SuccessHandler] Authentication success for registrationId={}",
          token.getAuthorizedClientRegistrationId());
      OAuth2User principal = token.getPrincipal();
      Map<String, Object> attributes = principal.getAttributes();

      AuthProvider provider = AuthProvider.valueOf(
          token.getAuthorizedClientRegistrationId().toUpperCase(Locale.ROOT));
      String providerUserId = resolveProviderUserId(provider, attributes);

      String email = extractEmail(provider, attributes);
      String name = extractDisplayName(provider, attributes);
      log.debug("[OAuth2SuccessHandler] Ensuring user for provider={}, providerUserId={} email={}",
          provider, providerUserId, email);
      User user = socialProvisioningService.ensureUser(provider, providerUserId, email, name);
      TokenResponse tokens = jwtTokenService.issueTokens(user);
      String fragment = String.format("#access_token=%s&refresh_token=%s",
              java.net.URLEncoder.encode(tokens.accessToken(), java.nio.charset.StandardCharsets.UTF_8),
              java.net.URLEncoder.encode(tokens.refreshToken(), java.nio.charset.StandardCharsets.UTF_8));
      String target = frontendBaseUrl + "/oauth/callback" + fragment;
      log.info("[OAuth2SuccessHandler] Redirecting userId={} to {}", user.getId(), target);
      response.sendRedirect(target);
      return;
    }

    response.sendRedirect(frontendBaseUrl + "/");
  }

  private String resolveProviderUserId(AuthProvider provider, Map<String, Object> attributes) {
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
