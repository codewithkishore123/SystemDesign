package com.chat.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.dto.ChatReceiptRequest;
import com.chat.dto.ReceiptResponse;
import com.chat.entity.Message;
import com.chat.enums.MessageStatus;
import com.chat.repository.ConversationParticipantRepository;
import com.chat.repository.MessageRepository;


@Service
public class MessageReceiptService {

    private final MessageRepository messageRepository;

    private final ConversationParticipantRepository participantRepository;

    private final SimpMessagingTemplate messagingTemplate;
    
    
    

    public MessageReceiptService(MessageRepository messageRepository,
			ConversationParticipantRepository participantRepository, SimpMessagingTemplate messagingTemplate) {
		super();
		this.messageRepository = messageRepository;
		this.participantRepository = participantRepository;
		this.messagingTemplate = messagingTemplate;
	}




	@Transactional
    public ReceiptResponse updateReceipt(
            UUID recipientId,
            ChatReceiptRequest request,
            MessageStatus requestedStatus) {

        Message message = messageRepository.findById(request.messageId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Message not found"));

        if (!message.getConversation().getId()
                .equals(request.conversationId())) {
            throw new IllegalArgumentException(
                    "Message does not belong to this conversation");
        }

        UUID senderId = message.getSender().getId();

        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException(
                    "Sender cannot update their own message receipt");
        }

        boolean isParticipant =
                participantRepository.existsByConversation_IdAndUser_Id(
                        request.conversationId(), recipientId);

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "You are not a participant in this conversation");
        }

        MessageStatus currentStatus = message.getStatus();

        if (requestedStatus == MessageStatus.DELIVERED) {

            if (currentStatus == MessageStatus.SENT) {
                message.setStatus(MessageStatus.DELIVERED);
            }

        } else if (requestedStatus == MessageStatus.READ) {

            if (currentStatus == MessageStatus.SENT
                    || currentStatus == MessageStatus.DELIVERED) {
                message.setStatus(MessageStatus.READ);
            }

        } else {
            throw new IllegalArgumentException(
                    "Unsupported receipt status");
        }

        message.setUpdatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        ReceiptResponse response = new ReceiptResponse(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                senderId,
                recipientId,
                savedMessage.getStatus(),
                savedMessage.getUpdatedAt()
        );

        // Notify the original sender about the receipt.
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/receipts",
                response
        );

        return response;
    }
}
