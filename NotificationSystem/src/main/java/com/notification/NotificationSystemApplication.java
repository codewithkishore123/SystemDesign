package com.notification;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationSystemApplication.class, args);
	}

	
	
	@Bean
	public ApplicationRunner checkRabbitConnection(
	        org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin) {

	    return args -> {
	        try {
	            System.out.println("Starting RabbitMQ declaration...");

	            rabbitAdmin.initialize();

	            System.out.println("RabbitMQ declaration completed successfully!");

	        } catch (Exception e) {
	            System.out.println("RabbitMQ declaration failed!");
	            e.printStackTrace();
	        }
	    };
	}
}
