package com.project.solvesync.global.security.auth;

/**
 * JWT 인증이 성공했을 때 SecurityContext에 주입되는 "내부 사용자" principal.
 *
 * - 내부 식별자(userId)가 핵심이며, username/email은 편의용(없을 수 있음)
 */
public record AuthUser(
        Long userId,
        String username,
        String email
) {
}
