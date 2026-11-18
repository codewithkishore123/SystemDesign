package com.chat.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.chat.config.RabbitMQConfig;
import com.chat.dto.ChatMessageEvent;



@Component
public class MessageEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    
    

    public MessageEventPublisher(RabbitTemplate rabbitTemplate) {
		super();
		this.rabbitTemplate = rabbitTemplate;
	}



	public void publish(ChatMessageEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                RabbitMQConfig.CHAT_ROUTING_KEY,
                event
        );
    }
}
