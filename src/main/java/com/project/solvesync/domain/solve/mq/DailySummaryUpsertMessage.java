package com.project.solvesync.domain.solve.mq;

import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * RabbitMQ에 적재되는 "일 단위 풀이 요약" 메시지.
 *
 * - 중복 전달(At-least-once) 가능하므로, DB upsert는 멱등(idempotent)해야 한다.
 */
public record DailySummaryUpsertMessage(
        @NotNull Long userPlatformAccountId,
        @NotNull LocalDate date,

        /** 총 해결 수(선택). null이면 difficultyStats 합으로 계산 */
        @Min(0) Integer solvedCount,

        @Valid List<@Valid DifficultyCount> difficultyStats,

        /** FastAPI가 생성한 시각(선택) */
        OffsetDateTime generatedAt
) {
    public record DifficultyCount(
            @Min(0) int difficulty,
            @Min(0) int solvedCount
    ) {}

    public static DailySummaryUpsertMessage from(DailySummaryDtos.UpsertItem item) {
        List<DifficultyCount> stats = (item.difficultyStats() == null)
                ? null
                : item.difficultyStats().stream()
                .map(d -> new DifficultyCount(d.difficulty(), d.solvedCount()))
                .toList();

        return new DailySummaryUpsertMessage(
                item.userPlatformAccountId(),
                item.date(),
                item.solvedCount(),
                stats,
                item.generatedAt()
        );
    }
}
