package com.recruitpro.controller;

import com.recruitpro.dto.request.InterviewFeedbackRequest;
import com.recruitpro.dto.request.RescheduleInterviewRequest;
import com.recruitpro.dto.request.ScheduleInterviewRequest;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.InterviewFeedbackResponse;
import com.recruitpro.dto.response.InterviewResponse;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Schedule, reschedule, cancel, and submit feedback")
public class InterviewController {

    private final InterviewService interviewService;

    // ── HR / Admin: schedule ──────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Schedule an interview (HR / Admin)")
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Interview scheduled",
                interviewService.scheduleInterview(principal.getId(), request)));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Reschedule an interview (HR / Admin)")
    public ResponseEntity<ApiResponse<InterviewResponse>> reschedule(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RescheduleInterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview rescheduled",
                interviewService.rescheduleInterview(id, principal.getId(), request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Cancel an interview (HR / Admin)")
    public ResponseEntity<ApiResponse<InterviewResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Interview cancelled",
                interviewService.cancelInterview(id, principal.getId(), reason)));
    }

    // ── Interviewer: my schedule + feedback ───────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasRole('INTERVIEWER')")
    @Operation(summary = "Get my assigned interviews (Interviewer)")
    public ResponseEntity<ApiResponse<Page<InterviewResponse>>> getMyInterviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size) {
        return ResponseEntity.ok(ApiResponse.success("Interviews fetched",
                interviewService.getMyInterviews(principal.getId(),
                        PageRequest.of(page, size, Sort.by("scheduledAt").ascending()))));
    }

    @GetMapping("/me/upcoming")
    @PreAuthorize("hasRole('INTERVIEWER')")
    @Operation(summary = "Get my upcoming interviews (Interviewer)")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getUpcoming(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming interviews fetched",
                interviewService.getUpcomingInterviews(principal.getId())));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('INTERVIEWER')")
    @Operation(summary = "Submit feedback for an interview (Interviewer)")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> submitFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InterviewFeedbackRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Feedback submitted",
                interviewService.submitFeedback(id, principal.getId(), request)));
    }

    // ── Shared reads ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get interview by ID")
    public ResponseEntity<ApiResponse<InterviewResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Interview fetched",
                interviewService.getInterviewById(id)));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get all interviews for a job application")
    public ResponseEntity<ApiResponse<Page<InterviewResponse>>> getByApplication(
            @PathVariable Long applicationId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Interviews fetched",
                interviewService.getInterviewsByApplication(applicationId,
                        PageRequest.of(page, size, Sort.by("scheduledAt").ascending()))));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get all interviews for a candidate")
    public ResponseEntity<ApiResponse<Page<InterviewResponse>>> getByCandidate(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Interviews fetched",
                interviewService.getInterviewsByCandidate(candidateId,
                        PageRequest.of(page, size, Sort.by("scheduledAt").ascending()))));
    }

    @GetMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get feedback for a specific interview")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> getFeedback(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Feedback fetched",
                interviewService.getFeedback(id)));
    }

    @GetMapping("/application/{applicationId}/feedback")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Get all feedback rounds for an application (HR / Admin)")
    public ResponseEntity<ApiResponse<List<InterviewFeedbackResponse>>> getAllFeedback(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success("Feedback fetched",
                interviewService.getAllFeedbackForApplication(applicationId)));
    }
}
