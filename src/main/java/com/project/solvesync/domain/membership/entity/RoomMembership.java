package com.project.solvesync.domain.membership.entity;

import com.project.solvesync.domain.room.entity.StudyRoom;
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
        name = "room_memberships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_user", columnNames = {"room_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_membership_room", columnList = "room_id"),
                @Index(name = "idx_membership_user", columnList = "user_id")
        }
)
public class RoomMembership extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    /**
     * MVP에서는 User FK 대신 Long만 저장 (OAuth/JWT 붙이면 User 연관으로 바꿔도 됨)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(nullable = false)
    private OffsetDateTime joinedAt;

    private OffsetDateTime leftAt;

    /**
     * 이 방에서 이 멤버가 참여하는 플랫폼 목록
     *
     * MVP 편의상 EAGER로 둠 (나중에 트래픽 커지면 LAZY + fetch join으로 최적화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "membership_platforms",
            joinColumns = @JoinColumn(name = "membership_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_membership_platform",
                    columnNames = {"membership_id", "platform"}
            )
    )
    private List<ParticipationPlatform> platforms = new ArrayList<>();

    public static RoomMembership owner(StudyRoom room, Long ownerId, List<ParticipationPlatform> platforms) {
        RoomMembership m = new RoomMembership();
        m.room = room;
        m.userId = ownerId;
        m.role = RoomRole.OWNER;
        m.status = MembershipStatus.ACTIVE;
        m.joinedAt = OffsetDateTime.now();
        if (platforms != null) m.platforms.addAll(platforms);
        return m;
    }

    public static RoomMembership member(StudyRoom room, Long userId, List<ParticipationPlatform> platforms) {
        RoomMembership m = new RoomMembership();
        m.room = room;
        m.userId = userId;
        m.role = RoomRole.MEMBER;
        m.status = MembershipStatus.ACTIVE;
        m.joinedAt = OffsetDateTime.now();
        if (platforms != null) m.platforms.addAll(platforms);
        return m;
    }

    public boolean isActive() {
        return this.status == MembershipStatus.ACTIVE;
    }

    public void leave() {
        if (this.status != MembershipStatus.ACTIVE) return;
        this.status = MembershipStatus.LEFT;
        this.leftAt = OffsetDateTime.now();
    }

    public void kick() {
        if (this.status != MembershipStatus.ACTIVE) return;
        this.status = MembershipStatus.KICKED;
        this.leftAt = OffsetDateTime.now();
    }

    public void ban() {
        if (this.status != MembershipStatus.ACTIVE) return;
        this.status = MembershipStatus.BANNED;
        this.leftAt = OffsetDateTime.now();
    }
}
