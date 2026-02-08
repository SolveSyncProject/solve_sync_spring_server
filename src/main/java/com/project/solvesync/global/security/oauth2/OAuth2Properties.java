package com.project.solvesync.global.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "solvesync.security.oauth2")
public record OAuth2Properties(
        List<String> authorizedRedirectUris
) {
}
