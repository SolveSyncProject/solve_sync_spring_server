package com.project.solvesync.domain.evaluation.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.evaluation.entity.EvaluationCellStatus;
import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationHistory;
import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationMemberResult;
import com.project.solvesync.domain.evaluation.repository.RoomRuleEvaluationHistoryRepository;
import com.project.solvesync.domain.membership.entity.MembershipStatus;
import com.project.solvesync.domain.membership.entity.ParticipationPlatform;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.domain.room.entity.RoomRulePlatform;
import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.solve.entity.DailySolveDifficultyStat;
import com.project.solvesync.domain.solve.entity.DailySolveSummary;
import com.project.solvesync.domain.solve.repository.DailySolveSummaryRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 방 규칙 수행 여부 판정(평가) 핵심 로직.
 *
 * - ACTIVE 방 중, "평가 가능한 기간"이 끝난 것들을 찾아서
 *   RoomRuleEvaluationHistory 1줄 + MemberResult(칸) N개를 저장한다.
 */
@Service
@RequiredArgsConstructor
public class RoomRuleEvaluator {

    private final StudyRoomRepository roomRepository;
    private final RoomMembershipRepository membershipRepository;
    private final DailySolveSummaryRepository dailySolveSummaryRepository;
    private final RoomRuleEvaluationHistoryRepository historyRepository;

    /** 기간 종료 후 이 정도 시간이 지난 뒤 평가(지연 수집/집계 여유) */
    @Value("${solvesync.evaluation.grace-hours:3}")
    private long graceHours;

    /**
     * 스케줄러에서 호출: ACTIVE 방 전체를 대상으로 "기한이 지난 period"를 catch-up 평가.
     */
    @Transactional
    public int evaluateAllDueRooms() {
        List<StudyRoom> rooms = roomRepository.findAllByStatusWithRuleAndPlatforms(RoomStatus.ACTIVE);
        int total = 0;
        for (StudyRoom room : rooms) {
            total += evaluateRoomDue(room);
        }
        return total;
    }

    /** 내부/개발 편의용: 특정 방만 catch-up 평가 */
    @Transactional
    public int evaluateRoomDueById(Long roomId) {
        StudyRoom room = roomRepository.findWithRuleAndPlatformsById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));
        if (room.getStatus() != RoomStatus.ACTIVE) return 0;
        return evaluateRoomDue(room);
    }

    /**
     * 특정 방에 대해 "지금 시각" 기준으로, 평가 가능한 period들을 모두 평가한다.
     *
     * @return 새로 평가한 period 개수
     */
    private int evaluateRoomDue(StudyRoom room) {
        if (room.getRule() == null) return 0;
        if (room.getEvaluationStartAt() == null) return 0;

        ZoneId zone = ZoneId.of(room.getTimezone());

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime graceCutoff = now.minusHours(graceHours);

        Integer maxIndex = historyRepository.findMaxPeriodIndex(room.getId());
        int nextIndex = (maxIndex == null) ? 0 : (maxIndex + 1);

        int evaluated = 0;
        while (true) {
            PeriodWindow w = calcPeriodWindow(room.getEvaluationStartAt(), room.getRule().getPeriodUnit(), nextIndex, zone);

            // 아직 기간이 끝나지 않았거나(grace 포함) 평가하면 안 됨
            if (w.endAt.isAfter(graceCutoff)) {
                break;
            }

            // idempotent: 이미 있으면 스킵하고 다음
            if (historyRepository.findByRoomIdAndPeriodIndex(room.getId(), nextIndex).isPresent()) {
                nextIndex++;
                continue;
            }

            evaluateOnePeriod(room, nextIndex, w);
            evaluated++;
            nextIndex++;
        }

        return evaluated;
    }

    private void evaluateOnePeriod(StudyRoom room, int periodIndex, PeriodWindow w) {
        ZoneId zone = ZoneId.of(room.getTimezone());

        // DailySolveSummary는 LocalDate 기반이므로, [start, end) 를 날짜 범위로 변환
        LocalDate from = w.startAt.atZoneSameInstant(zone).toLocalDate();
        LocalDate toExclusive = w.endAt.atZoneSameInstant(zone).toLocalDate();
        LocalDate toInclusive = toExclusive.minusDays(1);
        if (toInclusive.isBefore(from)) {
            // 이론상 없지만 방어
            toInclusive = from;
        }

        Map<Platform, RoomRulePlatform> platformRuleMap = room.getRulePlatforms().stream()
                .collect(Collectors.toMap(RoomRulePlatform::getPlatform, Function.identity(), (a, b) -> a));

        List<RoomMembership> memberships = membershipRepository.findAllWithPlatformsByRoomId(room.getId());
        List<Long> accountIds = memberships.stream()
                .flatMap(m -> m.getPlatforms().stream())
                .map(ParticipationPlatform::getUserPlatformAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, List<DailySolveSummary>> summaryByAccountId = new HashMap<>();
        if (!accountIds.isEmpty()) {
            List<DailySolveSummary> all = dailySolveSummaryRepository.findAllByUserPlatformAccountIdInAndDateBetween(accountIds, from, toInclusive);
            summaryByAccountId = all.stream().collect(Collectors.groupingBy(DailySolveSummary::getUserPlatformAccountId));
        }

        RoomRuleEvaluationHistory history = RoomRuleEvaluationHistory.create(
                room.getId(),
                room.getRule().getPeriodUnit(),
                periodIndex,
                w.startAt,
                w.endAt,
                OffsetDateTime.now(),
                room.getRule().getRequiredCount()
        );

        for (RoomMembership m : memberships) {
            RoomRuleEvaluationMemberResult cell = computeMemberCell(
                    m,
                    room.getRule().getRequiredCount(),
                    w.endAt,
                    zone,
                    from,
                    toInclusive,
                    platformRuleMap,
                    summaryByAccountId
            );
            history.addMemberResult(cell);
        }

        historyRepository.save(history);
    }

    private RoomRuleEvaluationMemberResult computeMemberCell(
            RoomMembership membership,
            int requiredCount,
            OffsetDateTime periodEndAt,
            ZoneId zone,
            LocalDate periodFrom,
            LocalDate periodTo,
            Map<Platform, RoomRulePlatform> platformRuleMap,
            Map<Long, List<DailySolveSummary>> summaryByAccountId
    ) {
        // 기간 종료 시점 기준으로 멤버십이 없으면: NOT_JOINED / LEFT / KICKED / BANNED
        if (membership.getJoinedAt() != null && !membership.getJoinedAt().isBefore(periodEndAt)) {
            return RoomRuleEvaluationMemberResult.of(membership.getUserId(), EvaluationCellStatus.NOT_JOINED, 0);
        }
        if (membership.getLeftAt() != null && !membership.getLeftAt().isAfter(periodEndAt)) {
            return RoomRuleEvaluationMemberResult.of(membership.getUserId(), toWithdrawalStatus(membership.getStatus()), 0);
        }

        // 기간 중 가입한 경우, 날짜 범위 보정(정확히는 timestamp 기반 fact가 필요하지만 MVP는 date 단위)
        LocalDate effectiveFrom = periodFrom;
        if (membership.getJoinedAt() != null) {
            LocalDate joinedDate = membership.getJoinedAt().atZoneSameInstant(zone).toLocalDate();
            if (joinedDate.isAfter(effectiveFrom)) effectiveFrom = joinedDate;
        }

        int achieved = 0;
        for (ParticipationPlatform p : membership.getPlatforms()) {
            Long accountId = p.getUserPlatformAccountId();
            if (accountId == null) continue;

            RoomRulePlatform pr = platformRuleMap.get(p.getPlatform());
            Integer tierMin = (pr != null) ? pr.getTierMin() : null;
            Integer tierMax = (pr != null) ? pr.getTierMax() : null;

            // (레거시 호환) 과거에는 "미지정"을 -1로 저장했을 수 있음
            if (tierMin != null && tierMin < 0) tierMin = null;
            if (tierMax != null && tierMax < 0) tierMax = null;

            List<DailySolveSummary> summaries = summaryByAccountId.getOrDefault(accountId, List.of());
            achieved += sumSolvedCount(summaries, effectiveFrom, periodTo, tierMin, tierMax);
        }

        EvaluationCellStatus status = (achieved >= requiredCount)
                ? EvaluationCellStatus.ACHIEVED
                : EvaluationCellStatus.FAILED;

        return RoomRuleEvaluationMemberResult.of(membership.getUserId(), status, achieved);
    }

    private int sumSolvedCount(
            List<DailySolveSummary> summaries,
            LocalDate from,
            LocalDate to,
            Integer tierMin,
            Integer tierMax
    ) {
        int sum = 0;
        for (DailySolveSummary s : summaries) {
            if (s.getDate().isBefore(from) || s.getDate().isAfter(to)) continue;

            // tier 범위가 없으면 총합
            if (tierMin == null && tierMax == null) {
                sum += s.getSolvedCount();
                continue;
            }

            // breakdown이 있으면 breakdown 기준으로 필터
            List<DailySolveDifficultyStat> stats = s.getDifficultyStats();
            if (stats != null && !stats.isEmpty()) {
                for (DailySolveDifficultyStat st : stats) {
                    int d = st.getDifficulty();
                    if (tierMin != null && d < tierMin) continue;
                    if (tierMax != null && d > tierMax) continue;
                    sum += st.getSolvedCount();
                }
            } else {
                // breakdown이 없다면 best-effort로 solvedCount 전체를 사용
                sum += s.getSolvedCount();
            }
        }
        return sum;
    }

    private EvaluationCellStatus toWithdrawalStatus(MembershipStatus status) {
        return switch (status) {
            case LEFT -> EvaluationCellStatus.LEFT;
            case KICKED -> EvaluationCellStatus.KICKED;
            case BANNED -> EvaluationCellStatus.BANNED;
            default -> EvaluationCellStatus.LEFT; // 방어
        };
    }

    private PeriodWindow calcPeriodWindow(OffsetDateTime evaluationStartAt, PeriodUnit unit, int periodIndex, ZoneId zone) {
        // ✅ 기간 경계는 "룸 타임존 기준 자정"으로 고정
        // - evaluationStartAt은 StudyRoom.activateNow()에서 '익일 0시'로 세팅됨
        // - 여기서는 LocalDate를 기준으로 periodIndex를 계산해 매 기간의 자정을 구한다.

        // (레거시 호환) 과거에는 evaluationStartAt=activatedAt(시/분/초 포함)일 수 있음.
        // 현재 정책은 "익일 0시부터"이므로, 자정이 아니면 baseDate를 하루 밀어준다.
        var zdt = evaluationStartAt.atZoneSameInstant(zone);
        LocalDate baseDate = zdt.toLocalDate();
        if (!zdt.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            baseDate = baseDate.plusDays(1);
        }

        LocalDate periodStartDate;
        LocalDate periodEndExclusiveDate;

        switch (unit) {
            case DAILY -> {
                periodStartDate = baseDate.plusDays(periodIndex);
                periodEndExclusiveDate = periodStartDate.plusDays(1);
            }
            case WEEKLY -> {
                periodStartDate = baseDate.plusWeeks(periodIndex);
                periodEndExclusiveDate = periodStartDate.plusWeeks(1);
            }
            case MONTHLY -> {
                periodStartDate = baseDate.plusMonths(periodIndex);
                periodEndExclusiveDate = periodStartDate.plusMonths(1);
            }
            default -> throw new IllegalStateException("Unsupported PeriodUnit: " + unit);
        }

        OffsetDateTime startAt = periodStartDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endAt = periodEndExclusiveDate.atStartOfDay(zone).toOffsetDateTime();
        return new PeriodWindow(startAt, endAt);
    }

    private record PeriodWindow(OffsetDateTime startAt, OffsetDateTime endAt) {}
}
