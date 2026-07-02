package com.recruitpro.dto.response;

import com.recruitpro.enums.InterviewStatus;
import com.recruitpro.enums.InterviewType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private Long interviewerUserId;
    private String interviewerName;
    private InterviewType interviewType;
    private InterviewStatus status;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String locationOrLink;
    private String notes;
    private boolean hasFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
