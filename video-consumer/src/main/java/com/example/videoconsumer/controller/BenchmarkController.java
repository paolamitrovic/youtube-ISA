package com.example.videoconsumer.controller;

import com.example.videoconsumer.mq.BenchmarkingService;
import com.example.videoconsumer.mq.BenchmarkingService.BenchmarkResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/benchmark")
public class BenchmarkController {
    
    @Autowired
    private BenchmarkingService benchmarkingService;
    
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBenchmark() {
        BenchmarkResult jsonResult = benchmarkingService.benchmarkJson();
        BenchmarkResult protobufResult = benchmarkingService.benchmarkProtobuf();
        
        Map<String, Object> results = new HashMap<>();
        Map<String, Object> json = new HashMap<>();
        json.put("format", jsonResult.getFormat());
        json.put("avgSerializationTimeMs", jsonResult.getAvgSerializationTimeMs());
        json.put("avgDeserializationTimeMs", jsonResult.getAvgDeserializationTimeMs());
        json.put("avgSendTimeMs", jsonResult.getAvgSendTimeMs());
        json.put("avgMessageSizeBytes", jsonResult.getAvgMessageSizeBytes());
        
        Map<String, Object> protobuf = new HashMap<>();
        protobuf.put("format", protobufResult.getFormat());
        protobuf.put("avgSerializationTimeMs", protobufResult.getAvgSerializationTimeMs());
        protobuf.put("avgDeserializationTimeMs", protobufResult.getAvgDeserializationTimeMs());
        protobuf.put("avgSendTimeMs", protobufResult.getAvgSendTimeMs());
        protobuf.put("avgMessageSizeBytes", protobufResult.getAvgMessageSizeBytes());
        
        results.put("json", json);
        results.put("protobuf", protobuf);
        
        // Calculate differences
        Map<String, Object> comparison = new HashMap<>();
        double serializationDiff = ((protobufResult.getAvgSerializationTimeMs() - jsonResult.getAvgSerializationTimeMs()) / jsonResult.getAvgSerializationTimeMs()) * 100;
        double deserializationDiff = ((protobufResult.getAvgDeserializationTimeMs() - jsonResult.getAvgDeserializationTimeMs()) / jsonResult.getAvgDeserializationTimeMs()) * 100;
        double sizeDiff = ((double)(protobufResult.getAvgMessageSizeBytes() - jsonResult.getAvgMessageSizeBytes()) / jsonResult.getAvgMessageSizeBytes()) * 100;
        
        comparison.put("serializationDiffPercent", Math.abs(serializationDiff));
        comparison.put("serializationFaster", serializationDiff < 0 ? "Protobuf" : "JSON");
        comparison.put("deserializationDiffPercent", Math.abs(deserializationDiff));
        comparison.put("deserializationFaster", deserializationDiff < 0 ? "Protobuf" : "JSON");
        comparison.put("sizeDiffPercent", Math.abs(sizeDiff));
        comparison.put("sizeSmaller", sizeDiff < 0 ? "Protobuf" : "JSON");
        
        results.put("comparison", comparison);
        
        // Also log to console
        benchmarkingService.runBenchmark();
        
        return ResponseEntity.ok(results);
    }
}
