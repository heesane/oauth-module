package ted.oauth.auth;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ted.oauth.social.AuthProvider;
import ted.oauth.social.SocialAccount;
import ted.oauth.social.SocialAccountRepository;
import ted.oauth.user.Gender;
import ted.oauth.user.User;
import ted.oauth.user.UserRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SocialUserProvisioningService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User ensureUser(AuthProvider provider,
                           String providerUserId,
                           String emailFromProvider,
                           String nameFromProvider) {
        log.info("[SocialProvisioning] ensureUser start provider={} providerUserId={} email={}", provider, providerUserId, emailFromProvider);
        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(existing -> {
                    existing.updateProfile(emailFromProvider, nameFromProvider);
                    log.info("[SocialProvisioning] existing social account found id={}", existing.getId());
                    return existing;
                })
                .orElseGet(() -> {
                    SocialAccount created = SocialAccount.create(provider, providerUserId, emailFromProvider, nameFromProvider);
                    socialAccountRepository.saveAndFlush(created);
                    log.info("[SocialProvisioning] new social account created id={}", created.getId());
                    return created;
                });

        User linked = socialAccount.getUser();
        if (linked != null) {
            log.info("[SocialProvisioning] social account already linked userId={}", linked.getId());
            socialAccountRepository.saveAndFlush(socialAccount);
            return linked;
        }

        String email = deriveEmail(provider, providerUserId, socialAccount.getEmail());
        User byEmail = userRepository.findByEmail(email).orElse(null);
        if (byEmail != null) {
            log.info("[SocialProvisioning] linking existing user by email userId={}", byEmail.getId());
            socialAccount.associateUser(byEmail);
            socialAccountRepository.saveAndFlush(socialAccount);
            return byEmail;
        }

        String name = deriveName(nameFromProvider, email);
        String nickname = generateUniqueNickname(name, provider);
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = User.create(
                email,
                name,
                nickname,
                encodedPassword,
                Gender.OTHER,
                LocalDate.now(),
                "",
                false
        );
        User savedUser = userRepository.saveAndFlush(newUser);
        log.info("[SocialProvisioning] new user created userId={} email={} nickname={}", savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
        socialAccount.associateUser(savedUser);
        socialAccountRepository.saveAndFlush(socialAccount);
        return savedUser;
    }

    private String deriveEmail(AuthProvider provider, String providerUserId, String originalEmail) {
        if (originalEmail != null && !originalEmail.isBlank()) {
            return originalEmail;
        }
        return provider.name().toLowerCase(Locale.ROOT) + "+" + providerUserId + "@social.local";
    }

    private String deriveName(String originalName, String fallbackEmail) {
        if (originalName != null && !originalName.isBlank()) {
            return originalName;
        }
        int atIndex = fallbackEmail.indexOf('@');
        return atIndex > 0 ? fallbackEmail.substring(0, atIndex) : fallbackEmail;
    }

    private String generateUniqueNickname(String baseName, AuthProvider provider) {
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        if (sanitized.isBlank()) {
            sanitized = provider.name().toLowerCase(Locale.ROOT);
        }
        String candidate = sanitized;
        int suffix = 1;
        while (userRepository.existsByNickname(candidate)) {
            candidate = sanitized + suffix;
            suffix++;
        }
        return candidate;
    }
}
