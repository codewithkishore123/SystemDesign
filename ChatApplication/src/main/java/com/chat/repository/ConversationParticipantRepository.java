package com.chat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.entity.ConversationParticipant;

public interface ConversationParticipantRepository
        extends JpaRepository<ConversationParticipant, UUID> {

    List<ConversationParticipant> findByConversation_Id(UUID conversationId);
    
    boolean existsByConversation_IdAndUser_Id(
            UUID conversationId,
            UUID userId);
}