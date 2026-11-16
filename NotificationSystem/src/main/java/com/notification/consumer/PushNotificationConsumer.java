package com.notification.consumer;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.notification.entity.Notification;
import com.notification.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class PushNotificationConsumer {
	
	  private final NotificationRepository notificationRepository;
	    private final ObjectMapper objectMapper;

	    public PushNotificationConsumer(
	            NotificationRepository notificationRepository,
	            ObjectMapper objectMapper) {

	        this.notificationRepository = notificationRepository;
	        this.objectMapper = objectMapper;
	    }

	    @RabbitListener(queues = "notification.push.queue")
	    public void consumePushNotification(String message) throws Exception {

	        System.out.println("Received push notification: " + message);

	        JsonNode event = objectMapper.readTree(message);

	        UUID notificationId = UUID.fromString(
	                event.path("notificationId").asText());

	        String destination = event.path("destination").asText();
	        String eventType = event.path("eventType").asText();

	        JsonNode payload = event.path("payload");

	        Notification notification = notificationRepository
	                .findById(notificationId)
	                .orElseThrow(() -> new IllegalStateException(
	                        "Notification not found: " + notificationId));

	        if (notification.getStatus() == NotificationStatus.SENT) {
	            System.out.println("Push already processed: " + notificationId);
	            return;
	        }

	        notification.setStatus(NotificationStatus.PROCESSING);
	        notificationRepository.save(notification);

	        // Simulated push notification provider
	        System.out.println("Sending push notification...");
	        System.out.println("Device token: " + destination);
	        System.out.println("Event: " + eventType);
	        System.out.println("Payload: " + payload.toPrettyString());

	        // Simulate successful provider acceptance
	        notification.setStatus(NotificationStatus.SENT);
	        notificationRepository.save(notification);

	        System.out.println(
	                "Push accepted by simulated provider: " + notificationId);
	    }

}
