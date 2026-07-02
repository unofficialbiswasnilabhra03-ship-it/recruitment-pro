package com.recruitpro.dto.response;

import com.recruitpro.enums.HireRecommendation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewFeedbackResponse {

    private Long id;
    private Long interviewId;
    private Long applicationId;
    private String interviewerName;
    private Integer technicalScore;
    private Integer communicationScore;
    private Integer problemSolvingScore;
    private Integer culturalFitScore;
    private Integer overallScore;
    private String strengths;
    private String weaknesses;
    private String detailedNotes;
    private HireRecommendation recommendation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
