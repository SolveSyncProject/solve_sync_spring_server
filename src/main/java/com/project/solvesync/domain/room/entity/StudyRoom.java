package com.project.solvesync.domain.room.entity;

import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_rooms",
        indexes = {
                @Index(name = "idx_room_invite_code", columnList = "invite_code"),
                @Index(name = "idx_room_public_list", columnList = "visibility, listed, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_invite_code", columnNames = "invite_code")
        }
)
public class StudyRoom extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomVisibility visibility;

    /**
     * PUBLIC 방 중 "게시판 노출 여부"
     */
    @Column(nullable = false)
    private boolean listed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "invite_code", nullable = false, length = 20)
    private String inviteCode;

    @Column(nullable = false, length = 60)
    private String timezone;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    private OffsetDateTime activatedAt;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RoomRule rule;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomRulePlatform> rulePlatforms = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMembership> memberships = new ArrayList<>();

    public static StudyRoom create(
            Long ownerId,
            String name,
            String description,
            RoomVisibility visibility,
            boolean listed,
            String timezone,
            OffsetDateTime startAt,
            String inviteCode
    ) {
        StudyRoom r = new StudyRoom();
        r.ownerId = ownerId;
        r.name = name;
        r.description = description;
        r.visibility = visibility;
        r.listed = listed;
        r.status = RoomStatus.DRAFT;
        r.timezone = timezone;
        r.startAt = startAt;
        r.inviteCode = inviteCode;
        return r;
    }

    public void attachRule(RoomRule rule) {
        this.rule = rule;
        rule.setRoom(this);
    }

    public void addRulePlatform(RoomRulePlatform rp) {
        rp.setRoom(this);
        this.rulePlatforms.add(rp);
    }

    public void addMembership(RoomMembership m) {
        this.memberships.add(m);
    }

    public void activateNow() {
        this.status = RoomStatus.ACTIVE;
        this.activatedAt = OffsetDateTime.now();

        // 정책: 활성화 시점이 startAt보다 늦으면 startAt을 now로 보정(과거 시작 방지)
        if (this.startAt.isBefore(this.activatedAt)) {
            this.startAt = this.activatedAt;
        }
    }
}
