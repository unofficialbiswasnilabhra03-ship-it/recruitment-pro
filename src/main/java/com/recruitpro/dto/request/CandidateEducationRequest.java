package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateEducationRequest {

    @NotBlank(message = "Institution name is required")
    @Size(max = 200)
    private String institution;

    @NotBlank(message = "Degree is required")
    @Size(max = 100)
    private String degree;

    @Size(max = 150)
    private String fieldOfStudy;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean currentlyStudying;

    @Size(max = 50)
    private String grade;

    private String description;
}
