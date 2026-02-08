package com.project.solvesync.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "solvesync.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTokenExpiresMinutes
) {
}
