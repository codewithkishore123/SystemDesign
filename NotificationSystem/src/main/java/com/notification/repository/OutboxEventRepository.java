package com.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.entity.OutboxEvent;
import com.notification.enums.OutboxStatus;

public interface OutboxEventRepository  extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
            OutboxStatus status
    );
}
