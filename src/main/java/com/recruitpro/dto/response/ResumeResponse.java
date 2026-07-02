package com.recruitpro.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {

    private Long id;
    private Long candidateId;
    private String originalFileName;
    private String contentType;
    private Long fileSizeBytes;
    private String storageBackend;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
