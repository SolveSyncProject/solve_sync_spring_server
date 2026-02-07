package com.project.solvesync.domain.evaluation.repository;

import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationMemberResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRuleEvaluationMemberResultRepository extends JpaRepository<RoomRuleEvaluationMemberResult, Long> {

    Optional<RoomRuleEvaluationMemberResult> findByHistory_IdAndUserId(Long historyId, Long userId);

    @Query("""
        select r
        from RoomRuleEvaluationMemberResult r
        join fetch r.history h
        where h.id in :historyIds
    """)
    List<RoomRuleEvaluationMemberResult> findAllByHistoryIds(@Param("historyIds") List<Long> historyIds);

    @Query("""
        select r
        from RoomRuleEvaluationMemberResult r
        join fetch r.history h
        where h.roomId = :roomId
        order by h.periodIndex asc
    """)
    List<RoomRuleEvaluationMemberResult> findAllByRoomIdWithHistory(@Param("roomId") Long roomId);
}
