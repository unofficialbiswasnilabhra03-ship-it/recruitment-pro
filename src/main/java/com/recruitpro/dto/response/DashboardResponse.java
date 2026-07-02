package com.recruitpro.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    // Jobs
    private long totalJobs;
    private long openJobs;
    private long closedJobs;
    private long draftJobs;

    // Applications
    private long totalApplications;
    private long newApplications;       // APPLIED
    private long shortlistedApplications;
    private long hiredCandidates;
    private long rejectedApplications;

    // Candidates
    private long totalCandidates;

    // Interviews
    private long totalInterviews;
    private long scheduledInterviews;
    private long completedInterviews;

    // People
    private long totalHrUsers;
    private long totalInterviewers;

    // Hiring funnel breakdown (status -> count)
    private Map<String, Long> applicationsByStatus;

    // Monthly hiring trend (month label -> hired count)
    private Map<String, Long> monthlyHiringTrend;
}
