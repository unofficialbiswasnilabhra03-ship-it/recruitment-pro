package com.recruitpro.dto.request;

import com.recruitpro.enums.ExperienceLevel;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CandidateProfileRequest {

    @Size(max = 300)
    private String headline;

    private String summary;

    private LocalDate dateOfBirth;

    @Size(max = 200)
    private String currentLocation;

    @Size(max = 200)
    private String preferredLocation;

    private ExperienceLevel experienceLevel;

    private Integer yearsOfExperience;

    private BigDecimal currentCtc;

    private BigDecimal expectedCtc;

    private Integer noticePeriodDays;

    @Size(max = 300)
    private String linkedinUrl;

    @Size(max = 300)
    private String githubUrl;

    @Size(max = 300)
    private String portfolioUrl;
}
