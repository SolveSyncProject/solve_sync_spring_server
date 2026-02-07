package com.project.solvesync.domain.evaluation.service;

import com.project.solvesync.domain.evaluation.dto.EvaluationDtos;
import com.project.solvesync.domain.evaluation.entity.EvaluationCellStatus;
import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationHistory;
import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationMemberResult;
import com.project.solvesync.domain.evaluation.repository.RoomRuleEvaluationHistoryRepository;
import com.project.solvesync.domain.evaluation.repository.RoomRuleEvaluationMemberResultRepository;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomRuleEvaluationServiceImpl implements RoomRuleEvaluationService {

    private final StudyRoomRepository roomRepository;
    private final RoomMembershipRepository membershipRepository;
    private final RoomRuleEvaluationHistoryRepository historyRepository;
    private final RoomRuleEvaluationMemberResultRepository resultRepository;
    private final RoomRuleEvaluator evaluator;

    @Override
    @Transactional(readOnly = true)
    public EvaluationDtos.RoomEvaluationMatrix getRoomEvaluationMatrix(Long roomId) {
        StudyRoom room = roomRepository.findWithRuleAndPlatformsById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));
        if (room.getRule() == null) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "방 규칙(rule)이 설정되어 있지 않습니다.");
        }

        List<RoomMembership> roster = membershipRepository.findAllWithPlatformsByRoomId(roomId);
        roster.sort(Comparator.comparing(RoomMembership::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        List<EvaluationDtos.MemberColumn> columns = roster.stream()
                .map(m -> new EvaluationDtos.MemberColumn(
                        m.getUserId(),
                        "userId=" + m.getUserId(),
                        m.getJoinedAt(),
                        m.getLeftAt()
                ))
                .toList();

        List<RoomRuleEvaluationHistory> histories = historyRepository.findAllByRoomIdOrderByPeriodIndexAsc(roomId);
        List<Long> historyIds = histories.stream().map(RoomRuleEvaluationHistory::getId).toList();
        Map<Long, Map<Long, RoomRuleEvaluationMemberResult>> resultMap = new HashMap<>();
        if (!historyIds.isEmpty()) {
            for (RoomRuleEvaluationMemberResult r : resultRepository.findAllByHistoryIds(historyIds)) {
                resultMap.computeIfAbsent(r.getHistory().getId(), k -> new HashMap<>()).put(r.getUserId(), r);
            }
        }

        // 빠진 칸이 있으면 membership 타임라인으로 최소한 NOT_JOINED/탈퇴사유는 채워준다.
        Map<Long, RoomMembership> membershipByUserId = roster.stream()
                .collect(Collectors.toMap(RoomMembership::getUserId, m -> m, (a, b) -> a));

        List<EvaluationDtos.PeriodRow> rows = new ArrayList<>();
        for (RoomRuleEvaluationHistory h : histories) {
            Map<Long, RoomRuleEvaluationMemberResult> byUser = resultMap.getOrDefault(h.getId(), Map.of());

            List<EvaluationDtos.Cell> cells = new ArrayList<>();
            for (EvaluationDtos.MemberColumn col : columns) {
                RoomRuleEvaluationMemberResult stored = byUser.get(col.userId());
                if (stored != null) {
                    cells.add(new EvaluationDtos.Cell(stored.getUserId(), stored.getStatus(), stored.getAchievedCount()));
                    continue;
                }

                RoomMembership m = membershipByUserId.get(col.userId());
                EvaluationCellStatus inferredStatus = inferStatusFromMembership(m, h.getPeriodEndAt());
                cells.add(new EvaluationDtos.Cell(col.userId(), inferredStatus, 0));
            }

            rows.add(new EvaluationDtos.PeriodRow(
                    h.getPeriodIndex(),
                    h.getPeriodStartAt(),
                    h.getPeriodEndAt(),
                    h.getEvaluatedAt(),
                    cells
            ));
        }

        return new EvaluationDtos.RoomEvaluationMatrix(
                roomId,
                room.getRule().getPeriodUnit(),
                room.getRule().getRequiredCount(),
                columns,
                rows
        );
    }

    @Override
    public EvaluationDtos.RunNowResponse runEvaluationNow(Long roomId) {
        int evaluated = evaluator.evaluateRoomDueById(roomId);
        return new EvaluationDtos.RunNowResponse(roomId, evaluated);
    }

    private EvaluationCellStatus inferStatusFromMembership(RoomMembership membership, java.time.OffsetDateTime periodEndAt) {
        if (membership == null) return EvaluationCellStatus.NOT_JOINED;

        if (membership.getJoinedAt() != null && !membership.getJoinedAt().isBefore(periodEndAt)) {
            return EvaluationCellStatus.NOT_JOINED;
        }
        if (membership.getLeftAt() != null && !membership.getLeftAt().isAfter(periodEndAt)) {
            return switch (membership.getStatus()) {
                case LEFT -> EvaluationCellStatus.LEFT;
                case KICKED -> EvaluationCellStatus.KICKED;
                case BANNED -> EvaluationCellStatus.BANNED;
                default -> EvaluationCellStatus.LEFT;
            };
        }
        // active였는데 저장이 누락된 경우(원래라면 없어야 함) -> UNKNOWN 대신 FAILED로 표시
        return EvaluationCellStatus.FAILED;
    }
}
