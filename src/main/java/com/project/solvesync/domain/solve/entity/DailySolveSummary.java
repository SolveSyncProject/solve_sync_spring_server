package com.project.solvesync.domain.solve.entity;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.global.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_solve_summaries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_summary_key", columnNames = {"user_platform_account_id", "summary_date"})
        },
        indexes = {
                @Index(name = "idx_daily_summary_key", columnList = "user_platform_account_id, summary_date"),
                @Index(name = "idx_daily_summary_date", columnList = "summary_date")
        }
)
public class DailySolveSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SolveSync 내부 플랫폼 계정 ID (UserPlatformAccount.id) */
    @Column(name = "user_platform_account_id", nullable = false)
    private Long userPlatformAccountId;

    /** 플랫폼 스냅샷(조회/집계 편의용) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    /** 기준 날짜 (로컬 날짜) */
    @Column(name = "summary_date", nullable = false)
    private LocalDate date;

    /** 해당 날짜 총 해결 수 */
    @Column(nullable = false)
    private int solvedCount;

    /** FastAPI가 생성한 요약의 생성 시각(선택) */
    private OffsetDateTime generatedAt;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailySolveDifficultyStat> difficultyStats = new ArrayList<>();

    private DailySolveSummary(Long userPlatformAccountId, Platform platform, LocalDate date) {
        this.userPlatformAccountId = Objects.requireNonNull(userPlatformAccountId);
        this.platform = Objects.requireNonNull(platform);
        this.date = Objects.requireNonNull(date);
        this.solvedCount = 0;
    }

    public static DailySolveSummary create(Long userPlatformAccountId, Platform platform, LocalDate date) {
        return new DailySolveSummary(userPlatformAccountId, platform, date);
    }

    /**
     * Upsert 적용
     * - solvedCount가 null이면 difficultyStats 합으로 자동 계산
     * - difficultyStats는 "전체 replace" 방식 (가장 단순 + idempotent)
     */
    public void applyUpsert(Integer solvedCount, List<DailySolveDifficultyStat> newStats, OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;

        // replace stats
        this.difficultyStats.clear();
        if (newStats != null) {
            for (DailySolveDifficultyStat s : newStats) {
                s.setSummary(this);
                this.difficultyStats.add(s);
            }
        }

        int sum = this.difficultyStats.stream().mapToInt(DailySolveDifficultyStat::getSolvedCount).sum();
        this.solvedCount = (solvedCount != null) ? solvedCount : sum;
    }
}
