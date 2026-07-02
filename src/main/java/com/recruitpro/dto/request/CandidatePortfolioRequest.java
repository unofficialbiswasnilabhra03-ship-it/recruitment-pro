package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CandidatePortfolioRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    @NotBlank(message = "URL is required")
    @URL(message = "Must be a valid URL")
    @Size(max = 500)
    private String url;

    @Size(max = 50)
    private String type; // PROJECT, BLOG, OPEN_SOURCE, CERTIFICATION
}
