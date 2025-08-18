package com.example.publisher.myapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private Long id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadTime;
}
