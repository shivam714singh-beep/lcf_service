    package com.example.publisher.myapp.service;

    import lombok.Data;

    @Data
    public class FileUploadedEvent {
        private Long id;
        private String fileName;
        private String fileType;
        private String gcsUrl;
    }
