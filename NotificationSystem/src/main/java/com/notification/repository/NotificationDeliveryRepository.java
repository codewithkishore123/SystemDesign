package com.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.entity.NotificationDelivery;
import com.notification.enums.NotificationChannel;

public interface NotificationDeliveryRepository
extends JpaRepository<NotificationDelivery, UUID> {

Optional<NotificationDelivery> findByNotificationIdAndChannel(
    UUID notificationId,
    NotificationChannel channel);
}
