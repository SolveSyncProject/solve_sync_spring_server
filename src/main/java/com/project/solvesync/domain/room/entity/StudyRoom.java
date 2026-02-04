package com.project.solvesync.domain.room.entity;

import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "study_rooms")
public class StudyRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 방장 userId */
    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomVisibility visibility;

    @Column(nullable = false)
    private boolean listed;

    @Column(nullable = false, length = 20)
    private String inviteCode;

    /** 룸 타임존 문자열(추후 기간 경계 계산에 사용 예정) */
    @Column(nullable = false, length = 40)
    private String timezone;

    /** 룸 설정값(정렬 기준 등으로 사용 가능), "평가 시작"은 evaluationStartAt이 담당 */
    @Column(nullable = false)
    private OffsetDateTime startAt;

    /** 방 ACTIVE 전환 시각 */
    private OffsetDateTime activatedAt;

    /**
     * 규칙 판정(집계) 시작 기준 시각.
     * 스펙: "ACTIVE 전환 이후 발생한 풀이 이벤트" 기준 → 활성화 시점으로 고정
     */
    @Column(name = "evaluation_start_at")
    private OffsetDateTime evaluationStartAt;

    /** ✅ RoomRule은 Entity이므로 Embedded 불가 → OneToOne으로 연결 */
    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RoomRule rule;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomRulePlatform> rulePlatforms = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMembership> memberships = new ArrayList<>();

    private StudyRoom(
            Long ownerId,
            String name,
            RoomVisibility visibility,
            boolean listed,
            String inviteCode,
            String timezone,
            OffsetDateTime startAt
    ) {
        this.ownerId = Objects.requireNonNull(ownerId);
        this.name = Objects.requireNonNull(name);
        this.visibility = Objects.requireNonNull(visibility);
        this.listed = listed;
        this.inviteCode = Objects.requireNonNull(inviteCode);
        this.timezone = Objects.requireNonNull(timezone);
        this.startAt = Objects.requireNonNull(startAt);
        this.status = RoomStatus.DRAFT;
    }

    public static StudyRoom create(
            Long ownerId,
            String name,
            RoomVisibility visibility,
            boolean listed,
            String inviteCode,
            String timezone,
            OffsetDateTime startAt
    ) {
        return new StudyRoom(ownerId, name, visibility, listed, inviteCode, timezone, startAt);
    }

    public void attachRule(RoomRule rule) {
        this.rule = Objects.requireNonNull(rule);
        rule.setRoom(this);
    }

    public void addRulePlatform(RoomRulePlatform rp) {
        rp.setRoom(this);
        this.rulePlatforms.add(rp);
    }

    public void addMembership(RoomMembership membership) {
        this.memberships.add(membership);
    }

    public void activateNow() {
        this.status = RoomStatus.ACTIVE;
        this.activatedAt = OffsetDateTime.now();
        this.evaluationStartAt = this.activatedAt;
        // startAt 보정은 제거: 평가는 evaluationStartAt 기준으로만 한다.
    }
}
