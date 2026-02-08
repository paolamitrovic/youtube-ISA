package com.example.backend.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue jsonQueue() {
        return new Queue(MessageProducer.JSON_QUEUE, false);
    }
    
    @Bean
    public Queue protobufQueue() {
        return new Queue(MessageProducer.PROTOBUF_QUEUE, false);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        // Use SimpleMessageConverter to handle raw strings and bytes
        return new SimpleMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // Use SimpleMessageConverter to send raw strings and bytes
        template.setMessageConverter(messageConverter());
        return template;
    }
}
