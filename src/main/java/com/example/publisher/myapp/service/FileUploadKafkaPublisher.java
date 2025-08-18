package com.example.publisher.myapp.service;

import com.example.publisher.myapp.entity.FileEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadKafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic = "file-uploads-topic";

    public void publishFileUploadedEvent(FileEntity entity) {
        try {
            FileUploadedEvent event = new FileUploadedEvent();
            event.setId(entity.getId());
            event.setFileName(entity.getFileName());
            event.setFileType(entity.getFileType());
            event.setGcsUrl(entity.getGcsUrl());
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, json);
            log.info("Published to Kafka: {}", json);
        } catch (Exception ex) {
            log.error("Kafka publish error", ex);
        }
    }
}
