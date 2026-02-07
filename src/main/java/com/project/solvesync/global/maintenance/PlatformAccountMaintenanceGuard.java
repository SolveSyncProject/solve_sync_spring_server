package com.project.solvesync.global.maintenance;

import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 플랫폼 계정 등록/수정(upsert)을 특정 시간대에 막기 위한 가드.
 *
 * 목적:
 * - FastAPI 수집 서버가 익일 02:00 ~ 02:30(예) 동안 전체 유저를 수집/정제할 때
 *   핸들 변경이 동시에 발생하면 수집/레지스트리 혼선이 생길 수 있음.
 * - SolveSync에서 01:30 ~ 03:00 사이를 점검 시간으로 정의하여, 해당 시간에는 upsert를 막는다.
 */
@Component
public class PlatformAccountMaintenanceGuard {

    @Value("${solvesync.maintenance.platform-account.enabled:true}")
    private boolean enabled;

    @Value("${solvesync.maintenance.platform-account.timezone:Asia/Seoul}")
    private String timezone;

    /** 예: 01:30 */
    @Value("${solvesync.maintenance.platform-account.start:01:30}")
    private String start;

    /** 예: 03:00 */
    @Value("${solvesync.maintenance.platform-account.end:03:00}")
    private String end;

    public void checkAvailable() {
        if (!enabled) return;

        LocalTime s = LocalTime.parse(start);
        LocalTime e = LocalTime.parse(end);
        LocalTime now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime();

        boolean inWindow;
        if (s.equals(e)) {
            // start==end이면 24시간 점검으로 간주
            inWindow = true;
        } else if (s.isBefore(e)) {
            // 같은 날 내 구간
            inWindow = !now.isBefore(s) && now.isBefore(e); // [start, end)
        } else {
            // 자정 넘어가는 구간 (예: 23:00 ~ 01:00)
            inWindow = !now.isBefore(s) || now.isBefore(e);
        }

        if (inWindow) {
            throw new BaseException(BaseResponseStatus.PLATFORM_ACCOUNT_MAINTENANCE);
        }
    }
}
