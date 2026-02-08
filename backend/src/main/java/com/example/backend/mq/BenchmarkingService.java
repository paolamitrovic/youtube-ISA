package com.example.backend.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BenchmarkingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkingService.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Autowired(required = false)
    private MessageProducer messageProducer;
    
    private static final int BENCHMARK_ITERATIONS = 50;
    
    public static class BenchmarkResult {
        private String format;
        private double avgSerializationTimeMs;
        private double avgDeserializationTimeMs;
        private double avgSendTimeMs;
        private long avgMessageSizeBytes;
        
        public BenchmarkResult(String format, double avgSerializationTimeMs, 
                              double avgDeserializationTimeMs, double avgSendTimeMs,
                              long avgMessageSizeBytes) {
            this.format = format;
            this.avgSerializationTimeMs = avgSerializationTimeMs;
            this.avgDeserializationTimeMs = avgDeserializationTimeMs;
            this.avgSendTimeMs = avgSendTimeMs;
            this.avgMessageSizeBytes = avgMessageSizeBytes;
        }
        
        // Getters
        public String getFormat() { return format; }
        public double getAvgSerializationTimeMs() { return avgSerializationTimeMs; }
        public double getAvgDeserializationTimeMs() { return avgDeserializationTimeMs; }
        public double getAvgSendTimeMs() { return avgSendTimeMs; }
        public long getAvgMessageSizeBytes() { return avgMessageSizeBytes; }
    }
    
    /**
     * Generates a random UploadEvent for benchmarking
     */
    private UploadEvent generateRandomEvent(int id) {
        Random random = new Random();
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            tags.add("tag" + random.nextInt(100));
        }
        
        return new UploadEvent(
            (long) id,
            "Video Title " + id,
            "Description for video " + id + " with some longer text content",
            (long) (random.nextInt(200 * 1024 * 1024) + 1024 * 1024), // 1MB to 200MB
            "user" + random.nextInt(1000),
            "user" + random.nextInt(1000) + "@example.com",
            LocalDateTime.now(),
            random.nextBoolean() ? "Location " + random.nextInt(100) : null,
            tags
        );
    }
    
    /**
     * Benchmarks JSON serialization, deserialization, and sending through RabbitMQ
     */
    public BenchmarkResult benchmarkJson() {
        List<Long> serializationTimes = new ArrayList<>();
        List<Long> deserializationTimes = new ArrayList<>();
        List<Long> sendTimes = new ArrayList<>();
        List<Long> messageSizes = new ArrayList<>();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            UploadEvent event = generateRandomEvent(i);
            
            try {
                // Serialization
                long startTime = System.nanoTime();
                String json = objectMapper.writeValueAsString(event);
                long serializationTime = (System.nanoTime() - startTime) / 1_000_000;
                serializationTimes.add(serializationTime);
                messageSizes.add((long) json.getBytes().length);
                
                // Deserialization
                startTime = System.nanoTime();
                objectMapper.readValue(json, UploadEvent.class);
                long deserializationTime = (System.nanoTime() - startTime) / 1_000_000;
                deserializationTimes.add(deserializationTime);
                
                // Send through RabbitMQ (if available)
                if (rabbitTemplate != null && messageProducer != null) {
                    startTime = System.nanoTime();
                    messageProducer.sendJsonMessage(event);
                    long sendTime = (System.nanoTime() - startTime) / 1_000_000;
                    sendTimes.add(sendTime);
                }
            } catch (Exception e) {
                logger.error("Error in JSON benchmark iteration {}", i, e);
            }
        }
        
        double avgSerialization = serializationTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgDeserialization = deserializationTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgSend = sendTimes.isEmpty() ? 0 : sendTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long avgSize = (long) messageSizes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        return new BenchmarkResult("JSON", avgSerialization, avgDeserialization, avgSend, avgSize);
    }
    
    /**
     * Benchmarks Protobuf serialization, deserialization, and sending through RabbitMQ
     */
    public BenchmarkResult benchmarkProtobuf() {
        List<Long> serializationTimes = new ArrayList<>();
        List<Long> deserializationTimes = new ArrayList<>();
        List<Long> sendTimes = new ArrayList<>();
        List<Long> messageSizes = new ArrayList<>();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            UploadEvent event = generateRandomEvent(i);
            
            try {
                // Serialization
                long startTime = System.nanoTime();
                UploadEventProto.UploadEvent protoEvent = event.toProtobuf();
                byte[] protobufBytes = protoEvent.toByteArray();
                long serializationTime = (System.nanoTime() - startTime) / 1_000_000;
                serializationTimes.add(serializationTime);
                messageSizes.add((long) protobufBytes.length);
                
                // Deserialization
                startTime = System.nanoTime();
                UploadEventProto.UploadEvent.parseFrom(protobufBytes);
                long deserializationTime = (System.nanoTime() - startTime) / 1_000_000;
                deserializationTimes.add(deserializationTime);
                
                // Send through RabbitMQ (if available)
                if (rabbitTemplate != null && messageProducer != null) {
                    startTime = System.nanoTime();
                    messageProducer.sendProtobufMessage(event);
                    long sendTime = (System.nanoTime() - startTime) / 1_000_000;
                    sendTimes.add(sendTime);
                }
            } catch (Exception e) {
                logger.error("Error in Protobuf benchmark iteration {}", i, e);
            }
        }
        
        double avgSerialization = serializationTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgDeserialization = deserializationTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgSend = sendTimes.isEmpty() ? 0 : sendTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long avgSize = (long) messageSizes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        return new BenchmarkResult("Protobuf", avgSerialization, avgDeserialization, avgSend, avgSize);
    }
    
    /**
     * Runs complete benchmark comparison
     */
    public void runBenchmark() {
        logger.info("Starting benchmark with {} iterations...", BENCHMARK_ITERATIONS);
        
        BenchmarkResult jsonResult = benchmarkJson();
        BenchmarkResult protobufResult = benchmarkProtobuf();
        
        logger.info("=== Benchmark Results ({} iterations) ===", BENCHMARK_ITERATIONS);
        logger.info("Format: {}", jsonResult.getFormat());
        logger.info("  Avg Serialization Time: {:.3f} ms", jsonResult.getAvgSerializationTimeMs());
        logger.info("  Avg Deserialization Time: {:.3f} ms", jsonResult.getAvgDeserializationTimeMs());
        if (jsonResult.getAvgSendTimeMs() > 0) {
            logger.info("  Avg Send Time (RabbitMQ): {:.3f} ms", jsonResult.getAvgSendTimeMs());
        }
        logger.info("  Avg Message Size: {} bytes ({} KB)", 
                jsonResult.getAvgMessageSizeBytes(), 
                jsonResult.getAvgMessageSizeBytes() / 1024.0);
        
        logger.info("Format: {}", protobufResult.getFormat());
        logger.info("  Avg Serialization Time: {:.3f} ms", protobufResult.getAvgSerializationTimeMs());
        logger.info("  Avg Deserialization Time: {:.3f} ms", protobufResult.getAvgDeserializationTimeMs());
        if (protobufResult.getAvgSendTimeMs() > 0) {
            logger.info("  Avg Send Time (RabbitMQ): {:.3f} ms", protobufResult.getAvgSendTimeMs());
        }
        logger.info("  Avg Message Size: {} bytes ({} KB)", 
                protobufResult.getAvgMessageSizeBytes(), 
                protobufResult.getAvgMessageSizeBytes() / 1024.0);
        
        logger.info("=== Comparison ===");
        double serializationDiff = ((protobufResult.getAvgSerializationTimeMs() - jsonResult.getAvgSerializationTimeMs()) / jsonResult.getAvgSerializationTimeMs()) * 100;
        double deserializationDiff = ((protobufResult.getAvgDeserializationTimeMs() - jsonResult.getAvgDeserializationTimeMs()) / jsonResult.getAvgDeserializationTimeMs()) * 100;
        double sendDiff = 0;
        if (jsonResult.getAvgSendTimeMs() > 0 && protobufResult.getAvgSendTimeMs() > 0) {
            sendDiff = ((protobufResult.getAvgSendTimeMs() - jsonResult.getAvgSendTimeMs()) / jsonResult.getAvgSendTimeMs()) * 100;
        }
        double sizeDiff = ((double)(protobufResult.getAvgMessageSizeBytes() - jsonResult.getAvgMessageSizeBytes()) / jsonResult.getAvgMessageSizeBytes()) * 100;
        
        logger.info("Serialization: Protobuf is {:.2f}% {} than JSON", 
                Math.abs(serializationDiff), serializationDiff < 0 ? "faster" : "slower");
        logger.info("Deserialization: Protobuf is {:.2f}% {} than JSON", 
                Math.abs(deserializationDiff), deserializationDiff < 0 ? "faster" : "slower");
        if (sendDiff != 0) {
            logger.info("Send Time: Protobuf is {:.2f}% {} than JSON", 
                    Math.abs(sendDiff), sendDiff < 0 ? "faster" : "slower");
        }
        logger.info("Message Size: Protobuf is {:.2f}% {} than JSON", 
                Math.abs(sizeDiff), sizeDiff < 0 ? "smaller" : "larger");
    }
}
