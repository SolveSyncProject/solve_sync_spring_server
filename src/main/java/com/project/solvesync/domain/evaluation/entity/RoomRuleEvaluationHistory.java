package com.project.solvesync.domain.evaluation.entity;

import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "room_rule_evaluation_histories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_period", columnNames = {"room_id", "period_index"})
        },
        indexes = {
                @Index(name = "idx_eval_room", columnList = "room_id"),
                @Index(name = "idx_eval_room_period", columnList = "room_id, period_index")
        }
)
public class RoomRuleEvaluationHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodUnit periodUnit;

    /**
     * evaluationStartAt을 0으로 두고 (startAt + index*periodUnit) 형태로 period 창을 계산한다.
     */
    @Column(name = "period_index", nullable = false)
    private int periodIndex;

    @Column(nullable = false)
    private OffsetDateTime periodStartAt;

    @Column(nullable = false)
    private OffsetDateTime periodEndAt;

    /**
     * 실제 평가가 수행된 시각(= periodEndAt + graceHours 이후)
     */
    @Column(nullable = false)
    private OffsetDateTime evaluatedAt;

    /**
     * 평가 당시 기준 requiredCount 스냅샷 (룰이 바뀌어도 과거 이력은 그대로 남김)
     */
    @Column(nullable = false)
    private int requiredCountSnapshot;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomRuleEvaluationMemberResult> memberResults = new ArrayList<>();

    public static RoomRuleEvaluationHistory create(
            Long roomId,
            PeriodUnit periodUnit,
            int periodIndex,
            OffsetDateTime periodStartAt,
            OffsetDateTime periodEndAt,
            OffsetDateTime evaluatedAt,
            int requiredCountSnapshot
    ) {
        RoomRuleEvaluationHistory h = new RoomRuleEvaluationHistory();
        h.roomId = roomId;
        h.periodUnit = periodUnit;
        h.periodIndex = periodIndex;
        h.periodStartAt = periodStartAt;
        h.periodEndAt = periodEndAt;
        h.evaluatedAt = evaluatedAt;
        h.requiredCountSnapshot = requiredCountSnapshot;
        return h;
    }

    public void addMemberResult(RoomRuleEvaluationMemberResult r) {
        r.bind(this);
        this.memberResults.add(r);
    }
}
