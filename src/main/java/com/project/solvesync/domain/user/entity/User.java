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
 * - OAuth 도입 전(MVP)에는 개발/테스트를 위해 "DEV 생성" 엔드포인트로 만들 수 있음.
 * - username(공개 식별자)은 선택값이며, 설정 시 유니크해야 함.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        },
        indexes = {
                @Index(name = "idx_users_username", columnList = "username")
        }
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * (MVP) 이메일은 선택. OAuth 도입 시 provider-sub/email 등으로 확장 예정.
     */
    @Column(length = 200)
    private String email;

    /**
     * 공개 식별자 (초대/검색에서 사용).
     * - null 가능
     * - 값이 존재하면 유니크
     */
    @Column(length = 50)
    private String username;

    private User(String email, String username) {
        this.email = normalize(email);
        this.username = normalize(username);
    }

    public static User create(String email, String username) {
        return new User(email, username);
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
