package com.chat.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.dto.ConversationResponse;
import com.chat.dto.ParticipantResponse;
import com.chat.entity.Conversation;
import com.chat.entity.ConversationParticipant;
import com.chat.entity.User;
import com.chat.enums.ConversationType;
import com.chat.repository.ConversationParticipantRepository;
import com.chat.repository.ConversationRepository;
import com.chat.repository.UserRepository;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository) {

        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConversationResponse createConversation(
            UUID senderId,
            UUID recipientId) {

        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException(
                    "You cannot create a conversation with yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sender not found"));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Recipient not found"));

        String directChatKey = generateDirectChatKey(
                senderId, recipientId);

        // Check whether the conversation already exists.
        var existingConversation =
                conversationRepository.findByDirectChatKey(directChatKey);

        if (existingConversation.isPresent()) {
            return buildConversationResponse(
                    existingConversation.get());
        }

        // Create a new conversation.
        Conversation conversation = new Conversation();

        conversation.setConversationType(ConversationType.DIRECT);
        conversation.setDirectChatKey(directChatKey);
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation =
                conversationRepository.save(conversation);

        // Add sender as a participant.
        ConversationParticipant senderParticipant =
                new ConversationParticipant();

        senderParticipant.setConversation(savedConversation);
        senderParticipant.setUser(sender);
        senderParticipant.setJoinedAt(LocalDateTime.now());

        // Add recipient as a participant.
        ConversationParticipant recipientParticipant =
                new ConversationParticipant();

        recipientParticipant.setConversation(savedConversation);
        recipientParticipant.setUser(recipient);
        recipientParticipant.setJoinedAt(LocalDateTime.now());

        participantRepository.saveAll(
                List.of(senderParticipant, recipientParticipant));

        return buildConversationResponse(savedConversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(
            UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        List<Conversation> conversations =
                conversationRepository.findAllByParticipantUserId(userId);

        return conversations.stream()
                .map(this::buildConversationResponse)
                .toList();
    }

    private ConversationResponse buildConversationResponse(
            Conversation conversation) {

        List<ConversationParticipant> participants =
                participantRepository.findByConversation_Id(
                        conversation.getId());

        List<ParticipantResponse> participantResponses =
                participants.stream()
                        .map(participant ->
                                new ParticipantResponse(
                                        participant.getUser().getId(),
                                        participant.getUser().getUsername()))
                        .toList();

        return new ConversationResponse(
                conversation.getId(),
                conversation.getConversationType(),
                participantResponses,
                conversation.getCreatedAt());
    }

    private String generateDirectChatKey(
            UUID firstUserId,
            UUID secondUserId) {

        List<UUID> sortedIds = List.of(firstUserId, secondUserId)
                .stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        return sortedIds.get(0) + ":" + sortedIds.get(1);
    }
}
