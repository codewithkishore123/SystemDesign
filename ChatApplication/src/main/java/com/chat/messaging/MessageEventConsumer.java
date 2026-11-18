package com.chat.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chat.config.RabbitMQConfig;
import com.chat.dto.ChatMessageEvent;


@Component
public class MessageEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    
    

    public MessageEventConsumer(SimpMessagingTemplate messagingTemplate) {
		super();
		this.messagingTemplate = messagingTemplate;
	}



	@RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consumeMessageEvent(ChatMessageEvent event) {

        System.out.println(
                "RabbitMQ received message event: "
                        + event.message().messageId()
        );

        for (var participantId : event.participantIds()) {

            messagingTemplate.convertAndSendToUser(
                    participantId.toString(),
                    "/queue/messages",
                    event.message()
            );
        }

        System.out.println("Message delivered to conversation participants");
    }
}
