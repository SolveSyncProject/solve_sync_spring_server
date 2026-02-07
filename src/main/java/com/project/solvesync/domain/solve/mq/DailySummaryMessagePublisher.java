package com.project.solvesync.domain.solve.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DailySummary upsert 메시지를 RabbitMQ에 publish.
 */
@Component
@RequiredArgsConstructor
public class DailySummaryMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${solvesync.mq.daily-summary.exchange}")
    private String exchange;

    @Value("${solvesync.mq.daily-summary.routing-key}")
    private String routingKey;

    public void publishUpsert(DailySummaryUpsertMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
