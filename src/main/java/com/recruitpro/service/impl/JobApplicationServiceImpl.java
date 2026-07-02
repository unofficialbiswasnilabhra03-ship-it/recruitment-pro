package com.recruitpro.service.impl;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.dto.request.ApplicationStatusUpdateRequest;
import com.recruitpro.dto.request.JobApplicationRequest;
import com.recruitpro.dto.response.JobApplicationResponse;
import com.recruitpro.entity.*;
import com.recruitpro.enums.ApplicationStatus;
import com.recruitpro.enums.NotificationType;
import com.recruitpro.exception.BadRequestException;
import com.recruitpro.exception.DuplicateResourceException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.exception.UnauthorizedException;
import com.recruitpro.repository.*;
import com.recruitpro.service.interfaces.EmailService;
import com.recruitpro.service.interfaces.JobApplicationService;
import com.recruitpro.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final CandidateRepository      candidateRepository;
    private final JobRepository            jobRepository;
    private final ResumeRepository         resumeRepository;
    private final EmailService             emailService;
    private final NotificationService      notificationService;

    // ── Candidate: Apply ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public JobApplicationResponse apply(Long userId, JobApplicationRequest request) {
        Candidate candidate = findCandidateByUserId(userId);
        Job job = jobRepository.findActiveById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        if (!"OPEN".equals(job.getStatus().name())) {
            throw new BadRequestException("This job is not accepting applications");
        }
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .candidate(candidate)
                .job(job)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        JobApplication saved = applicationRepository.save(application);
        emailService.sendApplicationReceivedEmail(saved);
        notificationService.createNotification(
                userId, NotificationType.APPLICATION_RECEIVED,
                "Application Submitted",
                "Your application for " + job.getTitle() + " has been received.",
                saved.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);

        return toResponse(saved, false);
    }

    // ── Candidate: Withdraw ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void withdraw(Long userId, Long applicationId) {
        Candidate candidate = findCandidateByUserId(userId);
        JobApplication application = findById(applicationId);

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You can only withdraw your own applications");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Application is already withdrawn");
        }
        if (application.getStatus() == ApplicationStatus.HIRED
                || application.getStatus() == ApplicationStatus.OFFER_EXTENDED) {
            throw new BadRequestException("Cannot withdraw after an offer has been extended");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    // ── Candidate: View own ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getMyApplications(Long userId, Pageable pageable) {
        Candidate candidate = findCandidateByUserId(userId);
        return applicationRepository.findByCandidateId(candidate.getId(), pageable)
                .map(a -> toResponse(a, false));
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getMyApplication(Long userId, Long applicationId) {
        Candidate candidate = findCandidateByUserId(userId);
        JobApplication app = findById(applicationId);
        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("Access denied");
        }
        return toResponse(app, false);
    }

    // ── HR: View + manage ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsByJob(Long jobId,
                                                              ApplicationStatus status,
                                                              Pageable pageable) {
        if (status != null) {
            return applicationRepository.findByJobIdAndStatus(jobId, status, pageable)
                    .map(a -> toResponse(a, true));
        }
        return applicationRepository.findByJobId(jobId, pageable).map(a -> toResponse(a, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsByCompany(Long companyId,
                                                                   ApplicationStatus status,
                                                                   Pageable pageable) {
        if (status != null) {
            return applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                    .map(a -> toResponse(a, true));
        }
        return applicationRepository.findByCompanyId(companyId, pageable)
                .map(a -> toResponse(a, true));
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(Long applicationId) {
        return toResponse(findById(applicationId), true);
    }

    @Override
    @Transactional
    public JobApplicationResponse updateStatus(Long applicationId,
                                                ApplicationStatusUpdateRequest request) {
        JobApplication application = findById(applicationId);
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.getStatus());
        if (request.getHrNotes()        != null) application.setHrNotes(request.getHrNotes());
        if (request.getRejectionReason() != null) application.setRejectionReason(request.getRejectionReason());

        JobApplication saved = applicationRepository.save(application);

        // Trigger emails on specific status transitions
        if (request.getStatus() == ApplicationStatus.SHORTLISTED && oldStatus != ApplicationStatus.SHORTLISTED) {
            emailService.sendShortlistEmail(saved);
            notificationService.createNotification(
                    saved.getCandidate().getUser().getId(),
                    NotificationType.APPLICATION_SHORTLISTED,
                    "Application Shortlisted",
                    "You've been shortlisted for " + saved.getJob().getTitle(),
                    saved.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
        } else if (request.getStatus() == ApplicationStatus.REJECTED) {
            emailService.sendRejectionEmail(saved);
            notificationService.createNotification(
                    saved.getCandidate().getUser().getId(),
                    NotificationType.REJECTION_EMAIL,
                    "Application Status Update",
                    "Your application for " + saved.getJob().getTitle() + " was not successful.",
                    saved.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
        } else if (request.getStatus() == ApplicationStatus.OFFER_EXTENDED) {
            emailService.sendOfferLetterEmail(saved);
            notificationService.createNotification(
                    saved.getCandidate().getUser().getId(),
                    NotificationType.OFFER_LETTER,
                    "Offer Extended",
                    "Congratulations! An offer has been extended for " + saved.getJob().getTitle(),
                    saved.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
        }

        return toResponse(saved, true);
    }

    @Override
    @Transactional
    public JobApplicationResponse shortlist(Long applicationId) {
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setStatus(ApplicationStatus.SHORTLISTED);
        return updateStatus(applicationId, req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsByCandidate(Long candidateId, Pageable pageable) {
        return applicationRepository.findByCandidateId(candidateId, pageable)
                .map(a -> toResponse(a, false));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JobApplication findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", id));
    }

    private Candidate findCandidateByUserId(Long userId) {
        return candidateRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate profile not found for user id: " + userId));
    }

    private JobApplicationResponse toResponse(JobApplication a, boolean isHrView) {
        boolean hasResume = resumeRepository.existsByCandidateId(a.getCandidate().getId());
        String downloadUrl = hasResume
                ? "/api/v1/resumes/candidate/" + a.getCandidate().getId() + "/download" : null;

        return JobApplicationResponse.builder()
                .id(a.getId())
                .jobId(a.getJob().getId())
                .jobTitle(a.getJob().getTitle())
                .companyName(a.getJob().getCompany().getName())
                .companyLogoUrl(a.getJob().getCompany().getLogoUrl())
                .candidateId(a.getCandidate().getId())
                .candidateName(a.getCandidate().getUser().getFirstName()
                        + " " + a.getCandidate().getUser().getLastName())
                .candidateEmail(a.getCandidate().getUser().getEmail())
                .status(a.getStatus())
                .coverLetter(a.getCoverLetter())
                // HR-only fields: null for candidate view
                .hrNotes(isHrView ? a.getHrNotes() : null)
                .rejectionReason(isHrView ? a.getRejectionReason() : null)
                .hasResume(hasResume)
                .resumeDownloadUrl(downloadUrl)
                .appliedAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
