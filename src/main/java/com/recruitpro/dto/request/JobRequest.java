package com.recruitpro.dto.request;

import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private String requirements;

    private String responsibilities;

    @Size(max = 200)
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private ExperienceLevel experienceLevel;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    @Positive
    private Integer openings = 1;

    private LocalDate applicationDeadline;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}
