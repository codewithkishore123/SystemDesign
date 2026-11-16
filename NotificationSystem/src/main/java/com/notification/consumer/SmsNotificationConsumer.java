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
public class SmsNotificationConsumer {
	
	 private final NotificationRepository notificationRepository;
	    private final ObjectMapper objectMapper;

	    public SmsNotificationConsumer(
	            NotificationRepository notificationRepository,
	            ObjectMapper objectMapper) {

	        this.notificationRepository = notificationRepository;
	        this.objectMapper = objectMapper;
	    }

	    @RabbitListener(queues = "notification.sms.queue")
	    public void consumeSmsNotification(String message) throws Exception {

	        System.out.println("Received SMS notification: " + message);

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
	            System.out.println("SMS already processed: " + notificationId);
	            return;
	        }

	        notification.setStatus(NotificationStatus.PROCESSING);
	        notificationRepository.save(notification);

	        // Simulated SMS provider
	        System.out.println("Sending SMS...");
	        System.out.println("Mobile number: " + destination);
	        System.out.println("Event: " + eventType);
	        System.out.println("Message: " + payload.toPrettyString());

	        // Simulate successful provider acceptance
	        notification.setStatus(NotificationStatus.SENT);
	        notificationRepository.save(notification);

	        System.out.println(
	                "SMS accepted by simulated provider: " + notificationId);
	    }

}
