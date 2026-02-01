package com.project.solvesync.domain.invitation.entity;

import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "room_invitations",
        indexes = {
                @Index(name = "idx_inv_room_status", columnList = "room_id,status"),
                @Index(name = "idx_inv_invitee", columnList = "invitee_user_id"),
                @Index(name = "idx_inv_inviter", columnList = "inviter_user_id")
        }
)
public class Invitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "invitee_user_id", nullable = false)
    private Long inviteeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    private Invitation(StudyRoom room, Long inviterUserId, Long inviteeUserId) {
        this.room = Objects.requireNonNull(room);
        this.inviterUserId = Objects.requireNonNull(inviterUserId);
        this.inviteeUserId = Objects.requireNonNull(inviteeUserId);
        this.status = InvitationStatus.PENDING;
    }

    public static Invitation create(StudyRoom room, Long inviterUserId, Long inviteeUserId) {
        return new Invitation(room, inviterUserId, inviteeUserId);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.decidedAt = OffsetDateTime.now();
    }

    public void decline() {
        this.status = InvitationStatus.DECLINED;
        this.decidedAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.decidedAt = OffsetDateTime.now();
    }
}
