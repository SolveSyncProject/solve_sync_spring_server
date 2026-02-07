package com.project.solvesync.global.integration.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * SolveSync -> 외부 수집 서버(FastAPI 등)로 "수집 타겟(유저 플랫폼 계정)" 변경사항을 푸시하는 클라이언트.
 *
 * MVP 원칙:
 * - SolveSync DB가 정합성의 단일 진실(Single Source of Truth)
 * - 외부 서버로의 전송 실패가 사용자 플로우를 막지 않도록, 예외를 삼키고 로그만 남긴다.
 */
@Slf4j
@Component
public class CollectorClient {

    @Value("${external.collector.enabled:false}")
    private boolean enabled;

    /** 예: http://localhost:8000 */
    @Value("${external.collector.base-url:}")
    private String baseUrl;

    /** 예: /internal/registry/platform-accounts/upsert */
    @Value("${external.collector.platform-account-upsert-path:/internal/registry/platform-accounts/upsert}")
    private String platformAccountUpsertPath;

    private final RestClient restClient = RestClient.builder().build();

    public void upsertPlatformAccount(PlatformAccountUpsertPayload payload) {
        if (!enabled) return;
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("external.collector.enabled=true 이지만 base-url이 비어있습니다. payload={}", payload);
            return;
        }

        String url = buildUrl(baseUrl, platformAccountUpsertPath);

        try {
            restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("외부 수집 서버로 플랫폼 계정 upsert 푸시 실패. url={}, payload={}, err={}", url, payload, e.toString());
        }
    }

    private String buildUrl(String baseUrl, String path) {
        String b = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String p = (path == null || path.isBlank()) ? "" : (path.startsWith("/") ? path : "/" + path);
        return b + p;
    }
}
