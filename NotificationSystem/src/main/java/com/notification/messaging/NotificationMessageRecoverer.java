package com.notification.messaging;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;

import com.notification.service.NotificationFailureService;

import tools.jackson.databind.ObjectMapper;

@Component
public class NotificationMessageRecoverer implements MessageRecoverer{
	
	private final NotificationFailureService failureService;
    private final ObjectMapper objectMapper;

    public NotificationMessageRecoverer(
            NotificationFailureService failureService,
            ObjectMapper objectMapper) {
        this.failureService = failureService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recover(Message message, Throwable cause) {

        try {
            String payload = new String(
                    message.getBody(),
                    StandardCharsets.UTF_8);

            Map<String, Object> event =
                    objectMapper.readValue(payload, Map.class);

            UUID notificationId = UUID.fromString(
                    event.get("notificationId").toString());

            String failureReason = cause.getMessage();

            failureService.markAsFailed(
                    notificationId,
                    failureReason);

            System.out.println(
                    "Notification permanently failed: " + notificationId);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Could not record notification failure", exception);
        }
    }

}
