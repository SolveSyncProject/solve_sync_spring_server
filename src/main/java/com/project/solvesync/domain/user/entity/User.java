package com.project.solvesync.domain.user.entity;

import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 서비스 내부 사용자.
 *
 * - Google OAuth2 로그인 성공 시(provider + providerUserId) 기준으로 유저를 upsert(없으면 생성)한다.
 * - username(공개 식별자)은 선택값이며, 설정 시 유니크해야 함.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_provider_sub", columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_provider_sub", columnList = "provider,provider_user_id")
        }
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OAuth2 Provider (ex: GOOGLE)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    /**
     * OAuth2 Provider user id (Google: sub)
     */
    @Column(name = "provider_user_id", length = 200)
    private String providerUserId;

    /**
     * (선택) 이메일
     * - Google 로그인 시 받아오면 저장
     */
    @Column(length = 200)
    private String email;

    /**
     * (선택) 프로필 이름(표시용)
     */
    @Column(length = 200)
    private String name;

    /**
     * (선택) 프로필 이미지 URL
     */
    @Column(length = 500)
    private String pictureUrl;

    /**
     * 공개 식별자 (초대/검색에서 사용).
     * - null 가능
     * - 값이 존재하면 유니크
     */
    @Column(length = 50)
    private String username;

    private User(AuthProvider provider,
                 String providerUserId,
                 String email,
                 String name,
                 String pictureUrl,
                 String username) {
        this.provider = provider;
        this.providerUserId = normalize(providerUserId);
        this.email = normalize(email);
        this.name = normalize(name);
        this.pictureUrl = normalize(pictureUrl);
        this.username = normalize(username);
    }

    /**
     * (Legacy/Dev) 직접 생성
     */
    public static User create(String email, String username) {
        return new User(null, null, email, null, null, username);
    }

    /**
     * OAuth2 로그인으로 생성
     */
    public static User createOAuth(AuthProvider provider,
                                   String providerUserId,
                                   String email,
                                   String name,
                                   String pictureUrl) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(providerUserId, "providerUserId");
        return new User(provider, providerUserId, email, name, pictureUrl, null);
    }

    /** OAuth2 프로필 최신값 반영(자주 변하는 값만) */
    public void updateOAuthProfile(String email, String name, String pictureUrl) {
        this.email = normalize(email);
        this.name = normalize(name);
        this.pictureUrl = normalize(pictureUrl);
    }

    public void updateUsername(String newUsername) {
        this.username = normalize(Objects.requireNonNull(newUsername, "username"));
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
