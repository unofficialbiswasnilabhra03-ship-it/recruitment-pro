package com.recruitpro.dto.response;

import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobStatus;
import com.recruitpro.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String responsibilities;
    private String location;
    private JobType jobType;
    private JobStatus status;
    private ExperienceLevel experienceLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Integer openings;
    private LocalDate applicationDeadline;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private Long postedByUserId;
    private String postedByName;
    private long applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
