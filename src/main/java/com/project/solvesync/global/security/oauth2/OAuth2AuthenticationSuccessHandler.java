package com.project.solvesync.global.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.solvesync.domain.auth.dto.AuthDtos;
import com.project.solvesync.global.exception.BaseResponse;
import com.project.solvesync.global.security.auth.AuthUser;
import com.project.solvesync.global.security.jwt.JwtProperties;
import com.project.solvesync.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Properties oauth2Properties;
    private final HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider tokenProvider,
                                              JwtProperties jwtProperties,
                                              OAuth2Properties oauth2Properties,
                                              HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository,
                                              ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.oauth2Properties = oauth2Properties;
        this.authRequestRepository = authRequestRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        Long userId = toLong(oauthUser.getAttribute("solvesync_user_id"));
        String username = (String) oauthUser.getAttribute("solvesync_username");
        String email = (String) oauthUser.getAttribute("solvesync_email");

        if (userId == null) {
            // 내부 userId가 없으면 정상 흐름이 아니므로 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getWriter(),
                    BaseResponse.failure(com.project.solvesync.global.exception.BaseResponseStatus.INTERNAL_SERVER_ERROR)
            );
            return;
        }

        AuthUser authUser = new AuthUser(userId, username, email);
        String accessToken = tokenProvider.createAccessToken(authUser);

        long expiresInSec = jwtProperties.accessTokenExpiresMinutes() * 60L;

        // OAuth2 핸드셰이크용 쿠키 정리
        authRequestRepository.removeAuthorizationRequest(request, response);

        // (옵션) redirect_uri가 있으면 그쪽으로 토큰을 붙여서 redirect
        String redirectUri = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);

        if (StringUtils.hasText(redirectUri) && isAuthorizedRedirectUri(redirectUri)) {
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token_type", "Bearer")
                    .queryParam("access_token", accessToken)
                    .queryParam("expires_in", expiresInSec)
                    .build(true)
                    .toUriString();
            response.sendRedirect(targetUrl);
            return;
        }

        // 기본: JSON 응답
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AuthDtos.TokenResponse data = new AuthDtos.TokenResponse(
                accessToken,
                "Bearer",
                expiresInSec,
                userId,
                username,
                email
        );

        objectMapper.writeValue(response.getWriter(), BaseResponse.success(data));
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        List<String> authorized = oauth2Properties.authorizedRedirectUris();
        if (authorized == null || authorized.isEmpty()) {
            // allow-list가 비어있으면 redirect 기능을 꺼두는 게 안전
            return false;
        }

        URI clientRedirect = URI.create(uri);
        for (String a : authorized) {
            URI allowedUri = URI.create(a);
            // host + port 기반으로 검증 (path는 프론트 라우팅 다양할 수 있어서 허용)
            if (allowedUri.getHost() != null
                    && allowedUri.getHost().equalsIgnoreCase(clientRedirect.getHost())
                    && getPort(allowedUri) == getPort(clientRedirect)) {
                return true;
            }
        }
        return false;
    }

    private int getPort(URI uri) {
        int port = uri.getPort();
        if (port != -1) return port;
        if ("https".equalsIgnoreCase(uri.getScheme())) return 443;
        return 80;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) return Long.parseLong(s);
        return null;
    }
}
