package com.project.solvesync.global.integration.collector;

import com.project.solvesync.domain.common.Platform;

import java.time.OffsetDateTime;

/**
 * 외부 수집 서버(FastAPI 등)에 "수집 대상 플랫폼 계정" 정보를 동기화하기 위한 payload.
 */
public record PlatformAccountUpsertPayload(
        Long userPlatformAccountId,
        Long userId,
        String username,
        Platform platform,
        String handle,
        OffsetDateTime updatedAt,
        boolean created
) {
}
