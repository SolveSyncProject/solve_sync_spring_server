package com.project.solvesync.domain.solve.mq;

import com.project.solvesync.domain.solve.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ에서 DailySummaryUpsertMessage를 소비하고 DB upsert를 수행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailySummaryUpsertConsumer {

    private final DailySummaryService dailySummaryService;

    @RabbitListener(
            queues = "${solvesync.mq.daily-summary.queue}",
            concurrency = "${solvesync.mq.daily-summary.concurrency:3}"
    )
    public void consume(DailySummaryUpsertMessage message) {
        try {
            dailySummaryService.upsertFromMessage(message);
        } catch (Exception e) {
            // throw 하면 기본 설정에서는 requeue 될 수 있음(재처리).
            log.warn("DailySummary upsert consumer failed. message={}, err={}", message, e.toString());
            throw e;
        }
    }
}
