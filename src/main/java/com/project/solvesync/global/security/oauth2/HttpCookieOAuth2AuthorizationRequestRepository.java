package com.project.solvesync.global.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.util.Base64;

public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQUEST";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "REDIRECT_URI";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .map(Cookie::getValue)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            // ✅ (Boot 4/Security 최신 시그니처) request,response 버전으로 제거
            removeAuthorizationRequest(request, response);
            return;
        }

        String serialized = serialize(authorizationRequest);
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);

        String redirectUri = request.getParameter(OAuth2ParameterNames.REDIRECT_URI);
        if (StringUtils.hasText(redirectUri)) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri, COOKIE_EXPIRE_SECONDS);
        }
    }

    /**
     * ✅ Boot 4 / Spring Security에서 AuthorizationRequestRepository가 요구하는 remove 시그니처
     * - authRequest 반환 + 쿠키 삭제
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);

        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);

        return authRequest;
    }

    private String serialize(OAuth2AuthorizationRequest obj) {
        byte[] bytes = SerializationUtils.serialize(obj);
        if (bytes == null) throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest");
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private OAuth2AuthorizationRequest deserialize(String cookieValue) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookieValue);
            Object obj = SerializationUtils.deserialize(bytes);
            return (OAuth2AuthorizationRequest) obj;
        } catch (Exception e) {
            return null;
        }
    }
}
