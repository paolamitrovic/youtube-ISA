package com.example.videoconsumer.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @RabbitListener(queues = "video.upload.json")
    public void receiveJsonMessage(String message) {
        try {
            UploadEvent event = objectMapper.readValue(message, UploadEvent.class);
            logger.info("=== Received JSON Message ===");
            logger.info("Video ID: {}", event.getVideoId());
            logger.info("Title: {}", event.getTitle());
            logger.info("Description: {}", event.getDescription());
            logger.info("Video Size: {} bytes ({} MB)", 
                    event.getVideoSizeBytes(), 
                    event.getVideoSizeBytes() != null ? event.getVideoSizeBytes() / (1024.0 * 1024.0) : 0);
            logger.info("Author: {} ({})", event.getAuthorUsername(), event.getAuthorEmail());
            logger.info("Created At: {}", event.getCreatedAt());
            logger.info("Location: {}", event.getLocation() != null ? event.getLocation() : "N/A");
            logger.info("Tags: {}", event.getTags());
            logger.info("=============================");
            
            // Here you can process the event (e.g., send notifications, update indexes, etc.)
            processUploadEvent(event);
            
        } catch (Exception e) {
            logger.error("Error processing JSON message", e);
        }
    }
    
    @RabbitListener(queues = "video.upload.protobuf")
    public void receiveProtobufMessage(byte[] message) {
        try {
            UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.parseFrom(message);
            UploadEvent event = UploadEvent.fromProtobuf(protoEvent);
            logger.info("=== Received Protobuf Message ===");
            logger.info("Video ID: {}", event.getVideoId());
            logger.info("Title: {}", event.getTitle());
            logger.info("Description: {}", event.getDescription());
            logger.info("Video Size: {} bytes ({} MB)", 
                    event.getVideoSizeBytes(), 
                    event.getVideoSizeBytes() != null ? event.getVideoSizeBytes() / (1024.0 * 1024.0) : 0);
            logger.info("Author: {} ({})", event.getAuthorUsername(), event.getAuthorEmail());
            logger.info("Created At: {}", event.getCreatedAt());
            logger.info("Location: {}", event.getLocation() != null ? event.getLocation() : "N/A");
            logger.info("Tags: {}", event.getTags());
            logger.info("================================");
            
            // Here you can process the event (e.g., send notifications, update indexes, etc.)
            processUploadEvent(event);
            
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing Protobuf message", e);
        } catch (Exception e) {
            logger.error("Error processing Protobuf message", e);
        }
    }
    
    /**
     * Process the upload event - implement your business logic here
     */
    private void processUploadEvent(UploadEvent event) {
        // Example: Send notification, update search index, generate thumbnails, etc.
        logger.info("Processing upload event for video: {}", event.getTitle());
        
        // TODO: Add your business logic here
        // - Send email notification
        // - Update search index
        // - Generate additional thumbnails
        // - Update analytics
        // etc.
    }
}
