package com.chat.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chat.enums.ConversationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType conversationType;

    // A canonical key for the two users in a direct conversation.
    // The unique constraint prevents duplicate direct conversations.
    @Column(nullable = false, unique = true)
    private String directChatKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Conversation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public String getDirectChatKey() {
        return directChatKey;
    }

    public void setDirectChatKey(String directChatKey) {
        this.directChatKey = directChatKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
