package com.recruitpro.dto.request;

import com.recruitpro.enums.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleInterviewRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Interviewer user ID is required")
    private Long interviewerUserId;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;

    @NotNull(message = "Scheduled date/time is required")
    @Future(message = "Interview must be scheduled in the future")
    private LocalDateTime scheduledAt;

    private Integer durationMinutes = 60;

    private String locationOrLink;

    private String notes;
}
