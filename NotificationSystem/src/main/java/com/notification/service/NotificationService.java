
package com.notification.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.notification.dto.NotificationDetailsResponse;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.dto.NotificationStatusResponse;
import com.notification.entity.Notification;
import com.notification.entity.OutboxEvent;
import com.notification.enums.NotificationStatus;
import com.notification.enums.OutboxStatus;
import com.notification.exception.ResourceNotFoundException;
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
            ObjectMapper objectMapper,
            OutboxEventRepository outboxEventRepository) {

        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    // Existing notification creation method
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

        // Create event payload
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

    // Get complete notification details
    public NotificationDetailsResponse getNotificationDetails(UUID id) {

        Notification notification = getNotificationById(id);

        return new NotificationDetailsResponse(
                notification.getId(),
                notification.getStatus(),
                notification.getChannel(),
                notification.getDestination(),
                notification.getEventType(),
                notification.getSourceService(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    // Get only notification status
    public NotificationStatusResponse getNotificationStatus(UUID id) {

        Notification notification = getNotificationById(id);

        return new NotificationStatusResponse(
                notification.getId(),
                notification.getStatus()
        );
    }

    // Get all notifications with pagination
    public Page<NotificationDetailsResponse> getAllNotifications(
            Pageable pageable) {

        return notificationRepository.findAll(pageable)
                .map(notification -> new NotificationDetailsResponse(
                        notification.getId(),
                        notification.getStatus(),
                        notification.getChannel(),
                        notification.getDestination(),
                        notification.getEventType(),
                        notification.getSourceService(),
                        notification.getCreatedAt(),
                        notification.getUpdatedAt()
                ));
    }

    // Common method to retrieve notification
    private Notification getNotificationById(UUID id) {

        return notificationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found: " + id
                        )
                );
    }
}
