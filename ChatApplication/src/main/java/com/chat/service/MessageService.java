package com.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.dto.MessageRequest;
import com.chat.dto.MessageResponse;
import com.chat.entity.Conversation;
import com.chat.entity.Message;
import com.chat.entity.User;
import com.chat.enums.MessageStatus;
import com.chat.repository.ConversationParticipantRepository;
import com.chat.repository.ConversationRepository;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageResponse sendMessage(
            UUID conversationId,
            UUID senderId,
            MessageRequest request) {

        // Verify conversation exists.
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found"));

        // Verify sender exists.
        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sender not found"));

        // Verify sender belongs to this conversation.
        boolean isParticipant = participantRepository
                .existsByConversation_IdAndUser_Id(
                        conversationId, senderId);

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "You are not a participant in this conversation");
        }

        // Create and persist message.
        LocalDateTime now = LocalDateTime.now();

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.content().trim());
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        Message savedMessage = messageRepository.save(message);

        return mapToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessageHistory(
            UUID conversationId,
            UUID userId,
            Pageable pageable) {

        if (!conversationRepository.existsById(conversationId)) {
            throw new IllegalArgumentException("Conversation not found");
        }

        boolean isParticipant = participantRepository
                .existsByConversation_IdAndUser_Id(
                        conversationId, userId);

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "You are not allowed to view this conversation");
        }

        return messageRepository
                .findByConversation_IdOrderByCreatedAtDesc(
                        conversationId, pageable)
                .map(this::mapToResponse);
    }

    private MessageResponse mapToResponse(Message message) {

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
    
    @Transactional(readOnly = true)
    public List<UUID> getConversationParticipantIds(UUID conversationId) {

        return participantRepository
                .findByConversation_Id(conversationId)
                .stream()
                .map(participant -> participant.getUser().getId())
                .toList();
    }
}
