package com.notification.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.entity.Notification;
import com.notification.entity.OutboxEvent;
import com.notification.enums.NotificationStatus;
import com.notification.enums.OutboxStatus;
import com.notification.repository.NotificationRepository;
import com.notification.repository.OutboxEventRepository;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class NotificationService {
	
	 private final NotificationRepository notificationRepository;
	 private final ObjectMapper objectMapper;
	 private final OutboxEventRepository outboxEventRepository;

	    public NotificationService(
	            NotificationRepository notificationRepository,
	            ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository) {

	        this.notificationRepository = notificationRepository;
	        this.objectMapper = objectMapper;
			this.outboxEventRepository = outboxEventRepository;
	    }

	    @Transactional
	    public NotificationResponse createNotification(
	            NotificationRequest request) {

	        var existing = notificationRepository
	                .findBySourceServiceAndIdempotencyKeyAndChannel(
	                        request.sourceService(),
	                        request.idempotencyKey(),
	                        request.channel()
	                );

	        if (existing.isPresent()) {
	            Notification notification = existing.get();

	            return new NotificationResponse(
	                    notification.getId(),
	                    notification.getStatus(),
	                    "Notification already exists"
	            );
	        }

	        Notification notification = new Notification();

	        notification.setSourceService(request.sourceService());
	        notification.setRecipientId(request.recipientId());
	        notification.setChannel(request.channel());
	        notification.setDestination(request.destination());
	        notification.setEventType(request.eventType());
	        notification.setTemplateCode(request.templateCode());
	        notification.setIdempotencyKey(request.idempotencyKey());
	        notification.setStatus(NotificationStatus.PENDING);

	       
	            notification.setPayload(
	                    objectMapper.writeValueAsString(request.payload())
	            );

	        Notification savedNotification =
	                notificationRepository.save(notification);
	        
	     // Create an event payload
	        Map<String, Object> eventPayload = new HashMap<>();

	        eventPayload.put("notificationId", savedNotification.getId());
	        eventPayload.put("channel", savedNotification.getChannel());
	        eventPayload.put("destination", savedNotification.getDestination());
	        eventPayload.put("eventType", savedNotification.getEventType());
	        eventPayload.put("templateCode", savedNotification.getTemplateCode());
	        eventPayload.put("payload", request.payload());

	        // Create outbox event
	        OutboxEvent outboxEvent = new OutboxEvent();

	        outboxEvent.setNotificationId(savedNotification.getId());
	        outboxEvent.setEventType("NOTIFICATION_CREATED");
	        outboxEvent.setPayload(
	                objectMapper.writeValueAsString(eventPayload)
	        );
	        outboxEvent.setStatus(OutboxStatus.NEW);

	        // Save outbox event
	        outboxEventRepository.save(outboxEvent);

	        return new NotificationResponse(
	                savedNotification.getId(),
	                savedNotification.getStatus(),
	                "Notification created successfully"
	        );
	    }

}
