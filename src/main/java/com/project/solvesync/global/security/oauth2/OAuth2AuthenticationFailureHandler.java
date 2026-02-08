package com.project.solvesync.global.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.solvesync.global.exception.BaseResponse;
import com.project.solvesync.global.exception.BaseResponseStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationFailureHandler(HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository,
                                              ObjectMapper objectMapper) {
        this.authRequestRepository = authRequestRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        authRequestRepository.removeAuthorizationRequest(request, response);

        BaseResponseStatus status = BaseResponseStatus.AUTH_UNAUTHORIZED;

        response.setStatus(status.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 너무 내부 메시지를 그대로 노출하지 않도록, 기본 메시지 + 간단한 힌트 정도만 제공
        String msg = status.getMessage();
        if (exception != null && exception.getMessage() != null && !exception.getMessage().isBlank()) {
            msg = status.getMessage() + " (" + exception.getMessage() + ")";
        }

        objectMapper.writeValue(response.getWriter(), BaseResponse.failure(status, msg));
    }
}
