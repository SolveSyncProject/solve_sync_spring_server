package com.project.solvesync.domain.user.entity;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 유저가 플랫폼(BOJ/Codeforces/...)에서 사용하는 "핸들(handle)"을 저장.
 *
 * - handle = 플랫폼 계정 식별자(아이디/닉네임/유저네임)
 * - MVP 단계에서는 User 엔티티를 두지 않으므로 userId(Long)로만 연결
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_platform_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_platform", columnNames = {"user_id", "platform"})
        },
        indexes = {
                @Index(name = "idx_user_platform", columnList = "user_id, platform")
        }
)
public class UserPlatformAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    @Column(nullable = false, length = 50)
    private String handle;

    /**
     * 추후 "계정 소유 검증"(예: 프로필 문구/랜덤 토큰 제출 등)을 도입할 경우 사용.
     * MVP에서는 null 유지.
     */
    private OffsetDateTime verifiedAt;

    public static UserPlatformAccount create(Long userId, Platform platform, String handle) {
        UserPlatformAccount a = new UserPlatformAccount();
        a.userId = userId;
        a.platform = platform;
        a.handle = normalize(handle);
        return a;
    }

    public void updateHandle(String handle) {
        this.handle = normalize(handle);
        // handle이 바뀌면 검증 상태는 무효로 보는 게 안전
        this.verifiedAt = null;
    }

    public void markVerifiedNow() {
        this.verifiedAt = OffsetDateTime.now();
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    private static String normalize(String handle) {
        return handle == null ? null : handle.trim();
    }
}
