package com.project.solvesync.domain.room.entity;

import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_rules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_room_rule_room", columnNames = "room_id")
})
public class RoomRule extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodUnit periodUnit;

    @Column(nullable = false)
    private int requiredCount;

    @Column(nullable = false)
    private boolean includeHolidays;

    public static RoomRule create(PeriodUnit periodUnit, int requiredCount, boolean includeHolidays) {
        RoomRule r = new RoomRule();
        r.periodUnit = periodUnit;
        r.requiredCount = requiredCount;
        r.includeHolidays = includeHolidays;
        return r;
    }

    void setRoom(StudyRoom room) {
        this.room = room;
    }
}
