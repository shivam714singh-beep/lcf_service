package com.example.publisher.myapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class GCSFileServiceImpl implements GCSFileService {

    // No-arg constructor - nothing injected!

    @Override
    public void uploadMultipartFile(MultipartFile file, String objectName) throws IOException {
        // No GCP action
        System.out.println("GCS upload skipped (integration is disabled): " + objectName);
    }

    @Override
    public boolean isBucketExists() {
        System.out.println("GCS bucket check skipped (integration is disabled)");
        return false; // Always false, disables bucket check in your app logic
    }

    @Override
    public String getBucketName() {
        return "gcp-disabled";
    }
}
