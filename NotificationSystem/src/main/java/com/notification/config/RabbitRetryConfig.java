package com.notification.config;


import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.notification.messaging.NotificationMessageRecoverer;

@Configuration
public class RabbitRetryConfig {

	 @Bean
	    public Advice rabbitRetryInterceptor(
	            NotificationMessageRecoverer notificationMessageRecoverer) {

	        return RetryInterceptorBuilder.stateless()
	                .maxRetries(2)
	                .backOffOptions(1000, 2.0, 5000)
	                .recoverer(notificationMessageRecoverer)
	                .build();
	    }

	    @Bean
	    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
	            SimpleRabbitListenerContainerFactoryConfigurer configurer,
	            ConnectionFactory connectionFactory,
	            @Qualifier("rabbitRetryInterceptor") Advice rabbitRetryInterceptor) {

	        SimpleRabbitListenerContainerFactory factory =
	                new SimpleRabbitListenerContainerFactory();

	        configurer.configure(factory, connectionFactory);
	        factory.setAdviceChain(rabbitRetryInterceptor);

	        return factory;
	    }
}