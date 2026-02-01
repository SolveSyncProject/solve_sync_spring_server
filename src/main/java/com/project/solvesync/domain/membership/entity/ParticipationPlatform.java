package com.project.solvesync.domain.membership.entity;

import com.project.solvesync.domain.common.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ParticipationPlatform {

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private Platform platform;

    /**
     * 추후 UserPlatformAccount를 붙이면 FK 저장
     * 지금은 null 허용(MVP)
     */
    @Column(name = "user_platform_account_id")
    private Long userPlatformAccountId;

    /**
     * "이 방에서 참여할 때의 핸들 스냅샷"
     * 추후 계정 연동 붙이면 채워 넣기.
     */
    @Column(name = "handle_snapshot", length = 60)
    private String handleSnapshot;

    public static ParticipationPlatform of(Platform platform) {
        return new ParticipationPlatform(platform, null, null);
    }
}
