package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateExperienceRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String companyName;

    @NotBlank(message = "Job title is required")
    @Size(max = 150)
    private String jobTitle;

    @Size(max = 200)
    private String location;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean currentlyWorking;

    private String description;
}
