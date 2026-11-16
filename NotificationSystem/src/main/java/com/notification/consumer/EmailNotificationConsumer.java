package com.notification.consumer;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.notification.entity.Notification;
import com.notification.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class EmailNotificationConsumer {
	
	 private final JavaMailSender mailSender;
	    private final NotificationRepository notificationRepository;
	    private final ObjectMapper objectMapper;

	    public EmailNotificationConsumer(
	            JavaMailSender mailSender,
	            NotificationRepository notificationRepository,
	            ObjectMapper objectMapper) {

	        this.mailSender = mailSender;
	        this.notificationRepository = notificationRepository;
	        this.objectMapper = objectMapper;
	    }

	    @RabbitListener(queues = "notification.email.queue")
	    public void consumeEmailNotification(String message) throws Exception {

	        System.out.println("Received email notification: " + message);

	        // 1. Deserialize RabbitMQ message
	        JsonNode event = objectMapper.readTree(message);

	        UUID notificationId = UUID.fromString(
	                event.path("notificationId").asText());

	        String destination = event.path("destination").asText();
	        String eventType = event.path("eventType").asText();
	        String templateCode = event.path("templateCode").asText();

	        JsonNode payload = event.path("payload");

	        // 2. Fetch original notification from PostgreSQL
	        Notification notification = notificationRepository
	                .findById(notificationId)
	                .orElseThrow(() -> new IllegalStateException(
	                        "Notification not found: " + notificationId));

	        // 3. Basic duplicate-processing protection
	        if (notification.getStatus() == NotificationStatus.SENT) {
	            System.out.println(
	                    "Notification already sent. Skipping: " + notificationId);
	            return;
	        }

	        // 4. Update status
	        notification.setStatus(NotificationStatus.PROCESSING);
	        notificationRepository.save(notification);

	        // 5. Prepare email
	        SimpleMailMessage email = new SimpleMailMessage();

	        email.setFrom("no-reply@notification.local");
	        email.setTo(destination);
	        email.setSubject("Notification: " + eventType);

	        email.setText(
	                "Hello,\n\n"
	                + "You have received a notification.\n\n"
	                + "Event: " + eventType + "\n"
	                + "Template: " + templateCode + "\n"
	                + "Details: " + payload.toPrettyString() + "\n\n"
	                + "Thank you.");

	        // 6. Send email through SMTP
	        mailSender.send(email);

	        // 7. Update status after SMTP accepts the message
	        notification.setStatus(NotificationStatus.SENT);
	        notificationRepository.save(notification);

	        System.out.println(
	                "Email sent successfully for notification: " + notificationId);
	    }

}
