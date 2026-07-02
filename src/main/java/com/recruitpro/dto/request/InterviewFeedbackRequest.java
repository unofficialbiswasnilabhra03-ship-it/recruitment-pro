package com.recruitpro.dto.request;

import com.recruitpro.enums.HireRecommendation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewFeedbackRequest {

    @Min(1) @Max(10)
    private Integer technicalScore;

    @Min(1) @Max(10)
    private Integer communicationScore;

    @Min(1) @Max(10)
    private Integer problemSolvingScore;

    @Min(1) @Max(10)
    private Integer culturalFitScore;

    @Min(1) @Max(10)
    private Integer overallScore;

    private String strengths;

    private String weaknesses;

    private String detailedNotes;

    @NotNull(message = "Hire recommendation is required")
    private HireRecommendation recommendation;
}
