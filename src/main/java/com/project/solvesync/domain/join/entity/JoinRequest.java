package com.project.solvesync.domain.join.entity;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "room_join_requests",
        indexes = {
                @Index(name = "idx_join_req_room_status", columnList = "room_id,status"),
                @Index(name = "idx_join_req_user", columnList = "user_id")
        }
)
public class JoinRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "intro_text", length = 1000)
    private String introText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JoinRequestStatus status;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "join_request_platforms",
            joinColumns = @JoinColumn(name = "join_request_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private Set<Platform> platforms = new LinkedHashSet<>();

    private JoinRequest(StudyRoom room, Long userId, String introText, Set<Platform> platforms) {
        this.room = Objects.requireNonNull(room, "room must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.introText = introText;
        this.status = JoinRequestStatus.PENDING;
        if (platforms != null) this.platforms.addAll(platforms);
    }

    public static JoinRequest create(StudyRoom room, Long userId, String introText, Set<Platform> platforms) {
        return new JoinRequest(room, userId, introText, platforms);
    }

    public boolean isPending() {
        return this.status == JoinRequestStatus.PENDING;
    }

    public void approve() {
        this.status = JoinRequestStatus.APPROVED;
        this.decidedAt = OffsetDateTime.now();
    }

    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
        this.decidedAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = JoinRequestStatus.CANCELLED;
        this.decidedAt = OffsetDateTime.now();
    }
}
