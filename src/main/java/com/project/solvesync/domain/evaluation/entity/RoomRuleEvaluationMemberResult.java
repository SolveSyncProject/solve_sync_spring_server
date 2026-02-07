package com.project.solvesync.domain.evaluation.entity;

import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 규칙 평가 "칸".
 *
 * - history_id + user_id가 유니크
 * - 탈퇴/강퇴/제재 사유까지 status(enum)로 저장
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "room_rule_evaluation_member_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_history_user", columnNames = {"history_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_result_history", columnList = "history_id"),
                @Index(name = "idx_result_user", columnList = "user_id")
        }
)
public class RoomRuleEvaluationMemberResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "history_id", nullable = false)
    private RoomRuleEvaluationHistory history;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EvaluationCellStatus status;

    /**
     * 해당 period에서 합산된 풀이 수(룰 평가의 근거치)
     * - 탈퇴/미가입 같은 경우는 0으로 둔다.
     */
    @Column(nullable = false)
    private int achievedCount;

    public static RoomRuleEvaluationMemberResult of(Long userId, EvaluationCellStatus status, int achievedCount) {
        RoomRuleEvaluationMemberResult r = new RoomRuleEvaluationMemberResult();
        r.userId = userId;
        r.status = status;
        r.achievedCount = achievedCount;
        return r;
    }

    void bind(RoomRuleEvaluationHistory history) {
        this.history = history;
    }
}
