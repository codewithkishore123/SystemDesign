package com.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	
	
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
	    return new RabbitAdmin(connectionFactory);
	}
	
	
	 // Exchange
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange("notification.exchange", true, false);
    }

    // Email queue
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("notification.email.queue").build();
    }

    // SMS queue
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable("notification.sms.queue").build();
    }

    // Push queue
    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable("notification.push.queue").build();
    }
    
    
    
    // Dead-letter queues
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable("notification.email.dlq").build();
    }

    @Bean
    public Queue smsDlq() {
        return QueueBuilder.durable("notification.sms.dlq").build();
    }

    @Bean
    public Queue pushDlq() {
        return QueueBuilder.durable("notification.push.dlq").build();
    }
    

    // Email binding
    @Bean
    public Binding emailBinding(
    		   @Qualifier("emailQueue") Queue emailQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(emailQueue)
                .to(notificationExchange)
                .with("notification.email");
    }

    // SMS binding
    @Bean
    public Binding smsBinding(
          @Qualifier("smsQueue")  Queue smsQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(smsQueue)
                .to(notificationExchange)
                .with("notification.sms");
    }

    // Push binding
    @Bean
    public Binding pushBinding(
            @Qualifier("pushQueue")Queue pushQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(pushQueue)
                .to(notificationExchange)
                .with("notification.push");
    }
    
    // DLQ bindings
    @Bean
    public Binding emailDlqBinding(
            @Qualifier("emailDlq") Queue queue,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(queue)
                .to(deadLetterExchange)
                .with("email.dlq");
    }

    @Bean
    public Binding smsDlqBinding(
            @Qualifier("smsDlq") Queue queue,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(queue)
                .to(deadLetterExchange)
                .with("sms.dlq");
    }

    @Bean
    public Binding pushDlqBinding(
            @Qualifier("pushDlq") Queue queue,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(queue)
                .to(deadLetterExchange)
                .with("push.dlq");
    }

}
