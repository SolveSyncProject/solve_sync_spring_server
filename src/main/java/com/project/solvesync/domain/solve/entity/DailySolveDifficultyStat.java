package com.project.solvesync.domain.solve.entity;

import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 하루 풀이 요약의 난이도 분포.
 *
 * - difficulty: FastAPI에서 환산한 난이도 점수(BOJ 기준). unknown이면 0.
 * - solvedCount: 해당 난이도에서 해결한 문제 수
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_solve_difficulty_stats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_diff", columnNames = {"daily_summary_id", "difficulty"})
        },
        indexes = {
                @Index(name = "idx_daily_diff_summary", columnList = "daily_summary_id"),
                @Index(name = "idx_daily_diff_difficulty", columnList = "difficulty")
        }
)
public class DailySolveDifficultyStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_summary_id", nullable = false)
    private DailySolveSummary summary;

    @Column(nullable = false)
    private int difficulty;

    @Column(nullable = false)
    private int solvedCount;

    private DailySolveDifficultyStat(int difficulty, int solvedCount) {
        this.difficulty = difficulty;
        this.solvedCount = solvedCount;
    }

    public static DailySolveDifficultyStat of(int difficulty, int solvedCount) {
        if (difficulty < 0) throw new IllegalArgumentException("difficulty must be >= 0");
        if (solvedCount < 0) throw new IllegalArgumentException("solvedCount must be >= 0");
        return new DailySolveDifficultyStat(difficulty, solvedCount);
    }

    void setSummary(DailySolveSummary summary) {
        this.summary = Objects.requireNonNull(summary);
    }
}
