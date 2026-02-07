package com.project.solvesync.domain.user.dto;

import com.project.solvesync.domain.common.Platform;

import java.time.OffsetDateTime;

/**
 * 외부 수집 서버(FastAPI 등)가 "현재 수집 대상"을 초기 동기화(pull)할 때 사용할 내부 DTO.
 *
 * - 너의 기본 운영은 "변경 시 push"지만,
 *   FastAPI 재기동/레지스트리 손상 복구를 위해 pull API도 남겨두는 게 안전하다.
 */
public class PlatformAccountRegistryDtos {

    public record RegistryItem(
            Long userPlatformAccountId,
            Long userId,
            String username,
            Platform platform,
            String handle,
            OffsetDateTime updatedAt
    ) {
    }
}
