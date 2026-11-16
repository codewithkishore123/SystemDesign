package com.notification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.notification.enums.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @Column(nullable = false)
	    private UUID notificationId;

	    @Column(nullable = false)
	    private String eventType;

	    @Column(columnDefinition = "TEXT", nullable = false)
	    private String payload;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private OutboxStatus status;

	    private int attempts = 0;

	    private LocalDateTime createdAt;

	    private LocalDateTime publishedAt;

	    @PrePersist
	    public void onCreate() {
	        createdAt = LocalDateTime.now();

	        if (status == null) {
	            status = OutboxStatus.NEW;
	        }
	    }

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public UUID getNotificationId() {
			return notificationId;
		}

		public void setNotificationId(UUID notificationId) {
			this.notificationId = notificationId;
		}

		public String getEventType() {
			return eventType;
		}

		public void setEventType(String eventType) {
			this.eventType = eventType;
		}

		public String getPayload() {
			return payload;
		}

		public void setPayload(String payload) {
			this.payload = payload;
		}

		public OutboxStatus getStatus() {
			return status;
		}

		public void setStatus(OutboxStatus status) {
			this.status = status;
		}

		public int getAttempts() {
			return attempts;
		}

		public void setAttempts(int attempts) {
			this.attempts = attempts;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public LocalDateTime getPublishedAt() {
			return publishedAt;
		}

		public void setPublishedAt(LocalDateTime publishedAt) {
			this.publishedAt = publishedAt;
		}

		@Override
		public String toString() {
			return "OutboxEvent [id=" + id + ", notificationId=" + notificationId + ", eventType=" + eventType
					+ ", payload=" + payload + ", status=" + status + ", attempts=" + attempts + ", createdAt="
					+ createdAt + ", publishedAt=" + publishedAt + "]";
		}
	    
	    

}
