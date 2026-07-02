package com.recruitpro.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompanyResponse {

    private Long id;
    private String name;
    private String description;
    private String website;
    private String industry;
    private String size;
    private String headquarters;
    private String logoUrl;
    private String linkedinUrl;
    private long activeJobCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
