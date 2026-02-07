package com.project.solvesync.domain.evaluation.repository;

import com.project.solvesync.domain.evaluation.entity.RoomRuleEvaluationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRuleEvaluationHistoryRepository extends JpaRepository<RoomRuleEvaluationHistory, Long> {

    @Query("""
        select max(h.periodIndex)
        from RoomRuleEvaluationHistory h
        where h.roomId = :roomId
    """)
    Integer findMaxPeriodIndex(@Param("roomId") Long roomId);

    Optional<RoomRuleEvaluationHistory> findByRoomIdAndPeriodIndex(Long roomId, int periodIndex);

    List<RoomRuleEvaluationHistory> findAllByRoomIdOrderByPeriodIndexAsc(Long roomId);

    @Query("""
        select h
        from RoomRuleEvaluationHistory h
        where h.roomId = :roomId
          and h.periodEndAt <= :until
        order by h.periodIndex desc
    """)
    List<RoomRuleEvaluationHistory> findLastEvaluatedPeriodIndex(@Param("roomId") Long roomId,
                                                                 @Param("until") OffsetDateTime until);
}
