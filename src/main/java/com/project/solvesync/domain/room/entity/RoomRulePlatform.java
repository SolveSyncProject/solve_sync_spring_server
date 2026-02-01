package com.project.solvesync.domain.room.entity;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "room_rule_platforms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_platform", columnNames = {"room_id", "platform"})
        }
)
public class RoomRulePlatform extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Platform platform;

    private Integer tierMin;
    private Integer tierMax;

    public static RoomRulePlatform create(Platform platform, Integer tierMin, Integer tierMax) {
        RoomRulePlatform p = new RoomRulePlatform();
        p.platform = platform;
        p.tierMin = tierMin;
        p.tierMax = tierMax;
        return p;
    }

    void setRoom(StudyRoom room) {
        this.room = room;
    }
}
