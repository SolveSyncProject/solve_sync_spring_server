package com.project.solvesync.domain.evaluation.dto;

import com.project.solvesync.domain.evaluation.entity.EvaluationCellStatus;
import com.project.solvesync.domain.room.entity.PeriodUnit;

import java.time.OffsetDateTime;
import java.util.List;

public class EvaluationDtos {

    /** 방의 평가 이력(표 형태로 띄우기 위한 응답) */
    public record RoomEvaluationMatrix(
            Long roomId,
            PeriodUnit periodUnit,
            int requiredCount,
            List<MemberColumn> columns,
            List<PeriodRow> rows
    ) {}

    public record MemberColumn(
            Long userId,
            String label,
            OffsetDateTime joinedAt,
            OffsetDateTime leftAt
    ) {}

    public record PeriodRow(
            int periodIndex,
            OffsetDateTime periodStartAt,
            OffsetDateTime periodEndAt,
            OffsetDateTime evaluatedAt,
            List<Cell> cells
    ) {}

    public record Cell(
            Long userId,
            EvaluationCellStatus status,
            int achievedCount
    ) {}

    /** 내부/개발용: 특정 방에 대해 "지금" 평가를 강제 실행 */
    public record RunNowResponse(
            Long roomId,
            int evaluatedPeriods
    ) {}
}
