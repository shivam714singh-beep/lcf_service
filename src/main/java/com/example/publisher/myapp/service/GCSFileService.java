package com.example.publisher.myapp.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface GCSFileService {
    void uploadMultipartFile(MultipartFile file, String objectName) throws IOException;
    boolean isBucketExists();
    String getBucketName();
}
