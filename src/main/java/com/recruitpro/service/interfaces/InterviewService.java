package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.InterviewFeedbackRequest;
import com.recruitpro.dto.request.RescheduleInterviewRequest;
import com.recruitpro.dto.request.ScheduleInterviewRequest;
import com.recruitpro.dto.response.InterviewFeedbackResponse;
import com.recruitpro.dto.response.InterviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InterviewService {

    InterviewResponse scheduleInterview(Long currentUserId, ScheduleInterviewRequest request);

    InterviewResponse rescheduleInterview(Long interviewId, Long currentUserId, RescheduleInterviewRequest request);

    InterviewResponse cancelInterview(Long interviewId, Long currentUserId, String reason);

    InterviewResponse getInterviewById(Long interviewId);

    Page<InterviewResponse> getInterviewsByApplication(Long applicationId, Pageable pageable);

    Page<InterviewResponse> getInterviewsByCandidate(Long candidateId, Pageable pageable);

    Page<InterviewResponse> getMyInterviews(Long interviewerUserId, Pageable pageable);

    List<InterviewResponse> getUpcomingInterviews(Long interviewerUserId);

    // Feedback
    InterviewFeedbackResponse submitFeedback(Long interviewId, Long interviewerUserId, InterviewFeedbackRequest request);

    InterviewFeedbackResponse getFeedback(Long interviewId);

    List<InterviewFeedbackResponse> getAllFeedbackForApplication(Long applicationId);
}
