package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApplicationRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String coverLetter;
}
