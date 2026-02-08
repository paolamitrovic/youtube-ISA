package com.example.backend.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    
    public static final String JSON_QUEUE = "video.upload.json";
    public static final String PROTOBUF_QUEUE = "video.upload.protobuf";
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Sends upload event as JSON message
     */
    public void sendJsonMessage(UploadEvent event) {
        try {
            // Serialize to JSON string
            String json = objectMapper.writeValueAsString(event);
            // Send as raw string using SimpleMessageConverter
            rabbitTemplate.convertAndSend(JSON_QUEUE, json);
            logger.info("Sent JSON message for video ID: {} (size: {} bytes)", 
                    event.getVideoId(), json.getBytes().length);
        } catch (Exception e) {
            logger.error("Error sending JSON message for video ID: {}", event.getVideoId(), e);
        }
    }
    
    /**
     * Sends upload event as Protobuf message
     */
    public void sendProtobufMessage(UploadEvent event) {
        try {
            UploadEventProto.UploadEvent protoEvent = event.toProtobuf();
            byte[] protobufBytes = protoEvent.toByteArray();
            // Send as raw bytes using SimpleMessageConverter
            rabbitTemplate.convertAndSend(PROTOBUF_QUEUE, protobufBytes);
            logger.info("Sent Protobuf message for video ID: {} (size: {} bytes)", 
                    event.getVideoId(), protobufBytes.length);
        } catch (Exception e) {
            logger.error("Error sending Protobuf message for video ID: {}", event.getVideoId(), e);
        }
    }
    
    /**
     * Sends upload event in both formats
     */
    public void sendMessage(UploadEvent event) {
        sendJsonMessage(event);
        sendProtobufMessage(event);
    }
}
