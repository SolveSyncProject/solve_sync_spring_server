package com.project.solvesync.domain.solve.dto;

import com.project.solvesync.domain.common.Platform;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class DailySummaryDtos {

    /** FastAPI -> SolveSync : MQ publish용 배치 */
    public record UpsertBatchRequest(
            @NotNull
            @Size(min = 1)
            List<@Valid UpsertItem> items
    ) { }

    /** ✅ A안: userPlatformAccountId 포함 (SolveSync가 resolve할 게 없음) */
    public record UpsertItem(
            @NotNull Long userPlatformAccountId,
            @NotNull LocalDate date,

            /** 총 해결 수(선택). null이면 difficultyStats 합으로 계산 */
            @Min(0) Integer solvedCount,

            /** 난이도(점수) 분포: unknown=0, 나머지는 FastAPI에서 점수로 환산 */
            @Valid List<@Valid DifficultyCount> difficultyStats,

            /** FastAPI에서 이 요약을 생성한 시각(선택) */
            OffsetDateTime generatedAt
    ) { }

    public record DifficultyCount(
            @Min(0) int difficulty,
            @Min(0) int solvedCount
    ) { }

    /** HTTP 요청은 성공했지만, DB upsert는 MQ consumer가 비동기로 수행 */
    public record UpsertBatchResponse(int acceptedCount) { }

    /** 저장된 데이터 확인용(Dev 조회) */
    public record SummaryView(
            Long userPlatformAccountId,
            Platform platform,
            LocalDate date,
            int solvedCount,
            OffsetDateTime generatedAt,
            List<DifficultyCount> difficultyStats
    ) { }
}
