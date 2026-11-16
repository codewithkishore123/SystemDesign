package com.notification.messaging;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.notification.entity.OutboxEvent;
import com.notification.enums.OutboxStatus;
import com.notification.repository.OutboxEventRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxPublisher {
	

    private static final String EXCHANGE = "notification.exchange";

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {

        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepository.findByStatusOrderByCreatedAtAsc(
                        OutboxStatus.NEW);

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {
            // Read the channel from the stored JSON payload
            JsonNode payload = objectMapper.readTree(event.getPayload());

            String channel = payload.path("channel").asText();

            // Determine routing key
            String routingKey = switch (channel) {
                case "EMAIL" -> "notification.email";
                case "SMS" -> "notification.sms";
                case "PUSH" -> "notification.push";
                default -> throw new IllegalArgumentException(
                        "Unsupported notification channel: " + channel);
            };

            // Track this individual message
            CorrelationData correlationData =
                    new CorrelationData(event.getId().toString());

            // Publish the JSON payload
            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    routingKey,
                    event.getPayload(),
                    correlationData);

            // Wait for broker confirmation
            CorrelationData.Confirm confirm =
                    correlationData.getFuture().get(5, TimeUnit.SECONDS);

            if (!confirm.isAck()) {
                throw new IllegalStateException(
                        "RabbitMQ rejected message: " + confirm.getReason());
            }

            // Check whether the message was returned as unroutable
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException(
                        "RabbitMQ returned an unroutable message");
            }

            // Update the Outbox only after successful confirmation
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());

            outboxRepository.save(event);

            System.out.println(
                    "Published Outbox event: " + event.getId()
                    + " | Routing key: " + routingKey);

        } catch (Exception e) {

            event.setAttempts(event.getAttempts() + 1);
            outboxRepository.save(event);

            System.err.println(
                    "Failed to publish Outbox event: " + event.getId());

            e.printStackTrace();
        }
    }

}
