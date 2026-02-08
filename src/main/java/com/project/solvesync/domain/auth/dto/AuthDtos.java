package com.project.solvesync.domain.auth.dto;

public class AuthDtos {

    /** OAuth2 로그인 성공 시 발급되는 JWT */
    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            Long userId,
            String username,
            String email
    ) {
    }
}
