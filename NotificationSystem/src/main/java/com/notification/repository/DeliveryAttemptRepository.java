package com.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.entity.DeliveryAttempt;

public interface DeliveryAttemptRepository
        extends JpaRepository<DeliveryAttempt, UUID> {

    List<DeliveryAttempt> findByDeliveryIdOrderByAttemptNumberAsc(
            UUID deliveryId);
}
