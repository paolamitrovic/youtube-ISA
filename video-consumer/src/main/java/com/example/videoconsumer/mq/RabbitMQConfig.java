package com.example.videoconsumer.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String JSON_QUEUE = "video.upload.json";
    public static final String PROTOBUF_QUEUE = "video.upload.protobuf";
    
    @Bean
    public Queue jsonQueue() {
        return new Queue(JSON_QUEUE, false);
    }
    
    @Bean
    public Queue protobufQueue() {
        return new Queue(PROTOBUF_QUEUE, false);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        // Use SimpleMessageConverter to handle raw strings and bytes
        return new SimpleMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // Use SimpleMessageConverter to receive raw strings and bytes
        template.setMessageConverter(messageConverter());
        return template;
    }
}
