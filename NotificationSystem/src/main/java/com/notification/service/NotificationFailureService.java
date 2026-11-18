package com.notification.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.notification.entity.Notification;
import com.notification.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;

@Service
public class NotificationFailureService {
	
	private final NotificationRepository notificationRepository;

    public NotificationFailureService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void markAsFailed(UUID notificationId, String reason) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found: " + notificationId));

        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailureReason(reason);
        notification.setFailedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

}
