package com.project.solvesync.global.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정
 *
 * - DailySummary upsert 메시지를 위한 Exchange/Queue/Binding 생성
 * - Jackson converter 등록(LocalDate / OffsetDateTime 직렬화 포함)
 */
@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Value("${solvesync.mq.daily-summary.exchange}")
    private String dailySummaryExchangeName;

    @Value("${solvesync.mq.daily-summary.queue}")
    private String dailySummaryQueueName;

    @Value("${solvesync.mq.daily-summary.routing-key}")
    private String dailySummaryRoutingKey;

    @Bean
    public DirectExchange dailySummaryExchange() {
        return new DirectExchange(dailySummaryExchangeName, true, false);
    }

    @Bean
    public Queue dailySummaryQueue() {
        return QueueBuilder.durable(dailySummaryQueueName).build();
    }

    @Bean
    public Binding dailySummaryBinding(Queue dailySummaryQueue, DirectExchange dailySummaryExchange) {
        return BindingBuilder.bind(dailySummaryQueue)
                .to(dailySummaryExchange)
                .with(dailySummaryRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
