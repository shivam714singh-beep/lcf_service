package com.example.publisher.myapp.service;

import com.example.publisher.myapp.entity.FileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventPublisherService {

    @Async
    public void publishFileUploadedEvent(FileEntity fileEntity) {

        log.info("Event publishing to Pub/Sub is disabled (no-op). No event sent for file: {}", fileEntity.getId());

    }
}
