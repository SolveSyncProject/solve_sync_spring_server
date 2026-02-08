package com.project.solvesync.global.security.oauth2;

import com.project.solvesync.domain.user.entity.AuthProvider;
import com.project.solvesync.domain.user.entity.User;
import com.project.solvesync.domain.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = resolveProvider(registrationId);

        Map<String, Object> attrs = new HashMap<>(oAuth2User.getAttributes());

        // Google standard claims
        String providerUserId = (String) attrs.get("sub");
        String email = (String) attrs.get("email");
        String name = (String) attrs.get("name");
        String picture = (String) attrs.get("picture");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new org.springframework.security.oauth2.core.OAuth2Error("invalid_user_info"),
                    "Missing provider user id (sub)"
            );
        }

        User user = userRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(existing -> {
                    existing.updateOAuthProfile(email, name, picture);
                    return existing;
                })
                .orElseGet(() -> User.createOAuth(provider, providerUserId, email, name, picture));

        userRepository.save(user);

        // success handler가 내부 userId를 꺼내 JWT로 만들 수 있도록 attribute에 심어준다.
        attrs.put("solvesync_user_id", user.getId());
        attrs.put("solvesync_username", user.getUsername());
        attrs.put("solvesync_email", user.getEmail());

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attrs, userNameAttributeName);
    }

    private AuthProvider resolveProvider(String registrationId) {
        if (registrationId == null) return AuthProvider.GOOGLE;
        return switch (registrationId.toLowerCase(Locale.ROOT)) {
            case "google" -> AuthProvider.GOOGLE;
            default -> AuthProvider.GOOGLE;
        };
    }
}
