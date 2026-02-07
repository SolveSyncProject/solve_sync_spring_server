package com.project.solvesync.domain.user.event;

/**
 * UserPlatformAccount 등록/수정(upsert) 이후 외부 수집 서버(FastAPI 등)로
 * "수집 대상 레지스트리"를 동기화하기 위한 이벤트.
 *
 * - 실제 전송은 AFTER_COMMIT 리스너에서 수행한다.
 */
public record UserPlatformAccountUpsertedEvent(
        Long userPlatformAccountId,
        boolean created
) {
}
