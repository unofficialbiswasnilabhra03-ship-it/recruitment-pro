package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @Size(max = 300)
    private String website;

    @Size(max = 100)
    private String industry;

    @Size(max = 50)
    private String size;

    @Size(max = 200)
    private String headquarters;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 300)
    private String linkedinUrl;
}
