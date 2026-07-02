package com.recruitpro.service.impl;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.dto.request.InterviewFeedbackRequest;
import com.recruitpro.dto.request.RescheduleInterviewRequest;
import com.recruitpro.dto.request.ScheduleInterviewRequest;
import com.recruitpro.dto.response.InterviewFeedbackResponse;
import com.recruitpro.dto.response.InterviewResponse;
import com.recruitpro.entity.*;
import com.recruitpro.enums.ApplicationStatus;
import com.recruitpro.enums.InterviewStatus;
import com.recruitpro.enums.NotificationType;
import com.recruitpro.exception.BadRequestException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.exception.UnauthorizedException;
import com.recruitpro.repository.*;
import com.recruitpro.service.interfaces.EmailService;
import com.recruitpro.service.interfaces.InterviewService;
import com.recruitpro.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository         interviewRepository;
    private final InterviewFeedbackRepository feedbackRepository;
    private final JobApplicationRepository    applicationRepository;
    private final UserRepository              userRepository;
    private final EmailService                emailService;
    private final NotificationService         notificationService;

    // ── Schedule ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(Long currentUserId, ScheduleInterviewRequest req) {
        JobApplication application = applicationRepository.findById(req.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", req.getApplicationId()));

        User interviewer = userRepository.findById(req.getInterviewerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getInterviewerUserId()));

        Interview interview = Interview.builder()
                .jobApplication(application)
                .interviewer(interviewer)
                .interviewType(req.getInterviewType())
                .scheduledAt(req.getScheduledAt())
                .durationMinutes(req.getDurationMinutes() != null ? req.getDurationMinutes() : 60)
                .locationOrLink(req.getLocationOrLink())
                .notes(req.getNotes())
                .status(InterviewStatus.SCHEDULED)
                .build();

        Interview saved = interviewRepository.save(interview);

        // Update application status
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        // Notify candidate
        emailService.sendInterviewInvitationEmail(saved);
        notificationService.createNotification(
                application.getCandidate().getUser().getId(),
                NotificationType.INTERVIEW_INVITATION,
                "Interview Scheduled",
                "Your interview for " + application.getJob().getTitle() + " has been scheduled.",
                saved.getId(), AppConstants.REF_TYPE_INTERVIEW);

        return toResponse(saved);
    }

    // ── Reschedule ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InterviewResponse rescheduleInterview(Long interviewId, Long currentUserId,
                                                  RescheduleInterviewRequest req) {
        Interview interview = findById(interviewId);
        guardNotCompleted(interview);

        interview.setScheduledAt(req.getNewScheduledAt());
        interview.setStatus(InterviewStatus.RESCHEDULED);
        if (req.getLocationOrLink() != null) interview.setLocationOrLink(req.getLocationOrLink());
        interview.setReminderSent(false); // reset so scheduler sends a new reminder

        Interview saved = interviewRepository.save(interview);

        emailService.sendInterviewRescheduledEmail(saved, req.getReason());
        notificationService.createNotification(
                interview.getJobApplication().getCandidate().getUser().getId(),
                NotificationType.INTERVIEW_RESCHEDULED,
                "Interview Rescheduled",
                "Your interview for " + interview.getJobApplication().getJob().getTitle()
                        + " has been rescheduled.",
                saved.getId(), AppConstants.REF_TYPE_INTERVIEW);

        return toResponse(saved);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InterviewResponse cancelInterview(Long interviewId, Long currentUserId, String reason) {
        Interview interview = findById(interviewId);
        guardNotCompleted(interview);

        interview.setStatus(InterviewStatus.CANCELLED);
        Interview saved = interviewRepository.save(interview);

        emailService.sendInterviewCancelledEmail(saved, reason);
        notificationService.createNotification(
                interview.getJobApplication().getCandidate().getUser().getId(),
                NotificationType.INTERVIEW_CANCELLED,
                "Interview Cancelled",
                "Your interview for " + interview.getJobApplication().getJob().getTitle()
                        + " has been cancelled.",
                saved.getId(), AppConstants.REF_TYPE_INTERVIEW);

        return toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long interviewId) {
        return toResponse(findById(interviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewResponse> getInterviewsByApplication(Long applicationId, Pageable pageable) {
        return interviewRepository.findByCandidateId(applicationId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewResponse> getInterviewsByCandidate(Long candidateId, Pageable pageable) {
        return interviewRepository.findByCandidateId(candidateId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewResponse> getMyInterviews(Long interviewerUserId, Pageable pageable) {
        User interviewer = userRepository.findById(interviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", interviewerUserId));
        return interviewRepository.findByInterviewer(interviewer, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getUpcomingInterviews(Long interviewerUserId) {
        return interviewRepository
                .findUpcomingForInterviewer(interviewerUserId, LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InterviewFeedbackResponse submitFeedback(Long interviewId, Long interviewerUserId,
                                                     InterviewFeedbackRequest req) {
        Interview interview = findById(interviewId);

        if (!interview.getInterviewer().getId().equals(interviewerUserId)) {
            throw new UnauthorizedException("Only the assigned interviewer can submit feedback");
        }
        if (feedbackRepository.existsByInterviewId(interviewId)) {
            throw new BadRequestException("Feedback has already been submitted for this interview");
        }

        InterviewFeedback feedback = InterviewFeedback.builder()
                .interview(interview)
                .technicalScore(req.getTechnicalScore())
                .communicationScore(req.getCommunicationScore())
                .problemSolvingScore(req.getProblemSolvingScore())
                .culturalFitScore(req.getCulturalFitScore())
                .overallScore(req.getOverallScore())
                .strengths(req.getStrengths())
                .weaknesses(req.getWeaknesses())
                .detailedNotes(req.getDetailedNotes())
                .recommendation(req.getRecommendation())
                .build();

        InterviewFeedback saved = feedbackRepository.save(feedback);

        // Mark interview as completed
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.save(interview);

        // Update application status
        JobApplication application = interview.getJobApplication();
        application.setStatus(ApplicationStatus.INTERVIEWED);
        applicationRepository.save(application);

        return toFeedbackResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewFeedbackResponse getFeedback(Long interviewId) {
        InterviewFeedback feedback = feedbackRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback not found for interview id: " + interviewId));
        return toFeedbackResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewFeedbackResponse> getAllFeedbackForApplication(Long applicationId) {
        return feedbackRepository.findByApplicationId(applicationId)
                .stream().map(this::toFeedbackResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Interview findById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));
    }

    private void guardNotCompleted(Interview interview) {
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify a completed interview");
        }
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Cannot modify a cancelled interview");
        }
    }

    private InterviewResponse toResponse(Interview i) {
        Candidate candidate = i.getJobApplication().getCandidate();
        return InterviewResponse.builder()
                .id(i.getId())
                .applicationId(i.getJobApplication().getId())
                .jobId(i.getJobApplication().getJob().getId())
                .jobTitle(i.getJobApplication().getJob().getTitle())
                .candidateId(candidate.getId())
                .candidateName(candidate.getUser().getFirstName() + " " + candidate.getUser().getLastName())
                .interviewerUserId(i.getInterviewer() != null ? i.getInterviewer().getId() : null)
                .interviewerName(i.getInterviewer() != null
                        ? i.getInterviewer().getFirstName() + " " + i.getInterviewer().getLastName() : null)
                .interviewType(i.getInterviewType())
                .status(i.getStatus())
                .scheduledAt(i.getScheduledAt())
                .durationMinutes(i.getDurationMinutes())
                .locationOrLink(i.getLocationOrLink())
                .notes(i.getNotes())
                .hasFeedback(i.getFeedback() != null)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private InterviewFeedbackResponse toFeedbackResponse(InterviewFeedback f) {
        Interview i = f.getInterview();
        String interviewerName = i.getInterviewer() != null
                ? i.getInterviewer().getFirstName() + " " + i.getInterviewer().getLastName() : null;
        return InterviewFeedbackResponse.builder()
                .id(f.getId())
                .interviewId(i.getId())
                .applicationId(i.getJobApplication().getId())
                .interviewerName(interviewerName)
                .technicalScore(f.getTechnicalScore())
                .communicationScore(f.getCommunicationScore())
                .problemSolvingScore(f.getProblemSolvingScore())
                .culturalFitScore(f.getCulturalFitScore())
                .overallScore(f.getOverallScore())
                .strengths(f.getStrengths())
                .weaknesses(f.getWeaknesses())
                .detailedNotes(f.getDetailedNotes())
                .recommendation(f.getRecommendation())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
