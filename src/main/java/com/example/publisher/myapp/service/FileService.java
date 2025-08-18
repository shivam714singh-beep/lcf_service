package com.example.publisher.myapp.service;

import com.example.publisher.myapp.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    FileEntity saveFile(MultipartFile file);
    FileEntity getFile(Long id);
    List<FileEntity> getAllFiles();
    void deleteFile(Long id);
}
