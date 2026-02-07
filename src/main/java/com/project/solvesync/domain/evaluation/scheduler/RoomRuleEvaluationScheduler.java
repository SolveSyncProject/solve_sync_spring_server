package com.project.solvesync.domain.evaluation.scheduler;

import com.project.solvesync.domain.evaluation.service.RoomRuleEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 규칙 수행 여부 판정 스케줄러.
 *
 * - period 단위(daily/weekly/monthly)로 "끝난" 기간들을 주기적으로 catch-up 평가
 * - 실제 평가 가능 여부는 RoomRuleEvaluator가 (periodEndAt + graceHours) 기준으로 판단
 */
@Component
@RequiredArgsConstructor
public class RoomRuleEvaluationScheduler {

    private final RoomRuleEvaluator evaluator;

    @Value("${solvesync.evaluation.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${solvesync.evaluation.scheduler.fixed-delay-ms:60000}")
    public void tick() {
        if (!enabled) return;
        evaluator.evaluateAllDueRooms();
    }
}
