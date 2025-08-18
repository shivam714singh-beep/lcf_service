package com.example.publisher.myapp.controller;

import com.example.publisher.myapp.entity.FileEntity;
import com.example.publisher.myapp.repo.FileRepository;
import com.example.publisher.myapp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("v1/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileRepository fileRepository;
    private final GCSFileService gcsFileService;
    private final EventPublisherService eventPublisherService;
    private final FileUploadKafkaPublisher fileUploadKafkaPublisher;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        FileEntity entity = null;
        ResponseEntity<String> response;

        try {
            String originalFileName = file.getOriginalFilename();
            String fileType = file.getContentType();

            entity = FileEntity.builder()
                    .fileName(originalFileName)
                    .fileType(fileType)
                    .data(file.getBytes())
                    .build();

            fileRepository.save(entity);

            Long id = entity.getId();
            String objectName = id + "_" + originalFileName;

            boolean gcsOk = false;
            try {
                gcsOk = gcsFileService.isBucketExists();
            } catch (Exception checkEx) {
                log.error("GCS bucket existence check failed: {}", checkEx.getMessage());
            }

            if (gcsOk) {
                try {
                    gcsFileService.uploadMultipartFile(file, objectName);
                    String bucket = gcsFileService.getBucketName();
                    String gcsUrl = "https://storage.googleapis.com/" + bucket + "/" + objectName;
                    entity.setGcsUrl(gcsUrl);
                    fileRepository.save(entity);

                    log.info("File saved in DB and uploaded to GCS. GCS URL: {}", gcsUrl);
                    response = ResponseEntity.ok("File saved & uploaded. GCS URL: " + gcsUrl);

                } catch (Exception uploadEx) {
                    log.error("GCS upload failed: {}", uploadEx.getMessage());
                    response = ResponseEntity.status(500)
                            .body("File saved in DB but upload to GCS FAILED: " + uploadEx.getMessage());
                }
            } else {
                log.warn("GCS bucket NOT available. File '{}' only saved in DB, not uploaded.", originalFileName);
                log.info("Produced message to Kafka topic: fileName={}, fileType={}, size={} bytes",
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getData().length
                );

                entity.setGcsUrl(null);
                fileRepository.save(entity);

                response = ResponseEntity.status(202)
                        .body("File name saved in DB only; GCS bucket not available, upload skipped.");
            }

            // Publish events after DB save
            eventPublisherService.publishFileUploadedEvent(entity);
            fileUploadKafkaPublisher.publishFileUploadedEvent(entity);

            return response;

        } catch (Exception e) {
            log.error("Upload failed entirely: {}", e.getMessage());
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        FileEntity file = fileService.getFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }

    @GetMapping
    public List<FileEntity> getAllFiles() {
        log.info("getAllFiles hit");
        return fileService.getAllFiles();
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        log.info("Deleted file with id {}", id);
        return ResponseEntity.ok("File deleted successfully");
    }
}
