package com.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chat.entity.Conversation;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByDirectChatKey(String directChatKey);

    @Query("""
        SELECT DISTINCT c
        FROM Conversation c
        JOIN ConversationParticipant p
            ON p.conversation.id = c.id
        WHERE p.user.id = :userId
        ORDER BY c.createdAt DESC
        """)
    List<Conversation> findAllByParticipantUserId(
            @Param("userId") UUID userId);
}