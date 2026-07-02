package com.recruitpro.service.impl;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.entity.*;
import com.recruitpro.enums.EmailStatus;
import com.recruitpro.enums.NotificationType;
import com.recruitpro.repository.EmailLogRepository;
import com.recruitpro.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender     mailSender;
    private final EmailLogRepository emailLogRepository;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ── Application emails ────────────────────────────────────────────────────

    @Async
    @Override
    public void sendApplicationReceivedEmail(JobApplication application) {
        String candidateName = fullName(application.getCandidate().getUser());
        String jobTitle      = application.getJob().getTitle();
        String companyName   = application.getJob().getCompany().getName();

        String body = String.format("""
                Dear %s,

                Thank you for applying for the position of %s at %s.

                We have received your application and our team will review it shortly.
                You will be notified about the next steps via email.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle, companyName);

        send(application.getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_APPLICATION_RECEIVED, jobTitle),
             body, NotificationType.APPLICATION_RECEIVED,
             application.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
    }

    @Async
    @Override
    public void sendShortlistEmail(JobApplication application) {
        String candidateName = fullName(application.getCandidate().getUser());
        String jobTitle      = application.getJob().getTitle();

        String body = String.format("""
                Dear %s,

                Congratulations! You have been shortlisted for the position of %s.

                Our team will be in touch shortly regarding the next steps in the selection process.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle);

        send(application.getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_SHORTLISTED, jobTitle),
             body, NotificationType.APPLICATION_SHORTLISTED,
             application.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
    }

    @Async
    @Override
    public void sendRejectionEmail(JobApplication application) {
        String candidateName = fullName(application.getCandidate().getUser());
        String jobTitle      = application.getJob().getTitle();

        String body = String.format("""
                Dear %s,

                Thank you for your interest in the %s position.

                After careful consideration, we have decided to move forward with other candidates
                whose qualifications more closely match our current requirements.

                We encourage you to apply for future openings that match your profile.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle);

        send(application.getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_REJECTION, jobTitle),
             body, NotificationType.REJECTION_EMAIL,
             application.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
    }

    @Async
    @Override
    public void sendOfferLetterEmail(JobApplication application) {
        String candidateName = fullName(application.getCandidate().getUser());
        String jobTitle      = application.getJob().getTitle();
        String companyName   = application.getJob().getCompany().getName();

        String body = String.format("""
                Dear %s,

                We are delighted to offer you the position of %s at %s.

                Our HR team will send you the formal offer letter with all the details
                including compensation, start date, and other terms within 24 hours.

                Please confirm your acceptance at your earliest convenience.

                Congratulations and welcome to the team!

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle, companyName);

        send(application.getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_OFFER_LETTER, jobTitle),
             body, NotificationType.OFFER_LETTER,
             application.getId(), AppConstants.REF_TYPE_JOB_APPLICATION);
    }

    // ── Interview emails ──────────────────────────────────────────────────────

    @Async
    @Override
    public void sendInterviewInvitationEmail(Interview interview) {
        String candidateName = fullName(interview.getJobApplication().getCandidate().getUser());
        String jobTitle      = interview.getJobApplication().getJob().getTitle();
        String scheduledAt   = interview.getScheduledAt().format(DT_FMT);

        String body = String.format("""
                Dear %s,

                You are invited to an interview for the position of %s.

                Interview Details:
                  Type     : %s
                  Date/Time: %s
                  Duration : %d minutes
                  Location : %s

                %s

                Please confirm your attendance by replying to this email.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle,
                interview.getInterviewType(), scheduledAt,
                interview.getDurationMinutes(),
                interview.getLocationOrLink(),
                interview.getNotes() != null ? "\nAdditional Notes:\n" + interview.getNotes() : "");

        send(interview.getJobApplication().getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_INTERVIEW_INVITATION, jobTitle),
             body, NotificationType.INTERVIEW_INVITATION,
             interview.getId(), AppConstants.REF_TYPE_INTERVIEW);
    }

    @Async
    @Override
    public void sendInterviewReminderEmail(Interview interview) {
        String candidateName = fullName(interview.getJobApplication().getCandidate().getUser());
        String jobTitle      = interview.getJobApplication().getJob().getTitle();

        String body = String.format("""
                Dear %s,

                This is a reminder that your interview for %s is scheduled for:

                  Date/Time: %s
                  Location : %s

                Please be ready on time.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle,
                interview.getScheduledAt().format(DT_FMT),
                interview.getLocationOrLink());

        send(interview.getJobApplication().getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_INTERVIEW_REMINDER, jobTitle),
             body, NotificationType.INTERVIEW_REMINDER,
             interview.getId(), AppConstants.REF_TYPE_INTERVIEW);
    }

    @Async
    @Override
    public void sendInterviewRescheduledEmail(Interview interview, String reason) {
        String candidateName = fullName(interview.getJobApplication().getCandidate().getUser());
        String jobTitle      = interview.getJobApplication().getJob().getTitle();

        String body = String.format("""
                Dear %s,

                Your interview for %s has been rescheduled.

                New Schedule:
                  Date/Time: %s
                  Duration : %d minutes
                  Location : %s

                Reason: %s

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle,
                interview.getScheduledAt().format(DT_FMT),
                interview.getDurationMinutes(),
                interview.getLocationOrLink(),
                reason != null ? reason : "N/A");

        send(interview.getJobApplication().getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_INTERVIEW_RESCHEDULED, jobTitle),
             body, NotificationType.INTERVIEW_RESCHEDULED,
             interview.getId(), AppConstants.REF_TYPE_INTERVIEW);
    }

    @Async
    @Override
    public void sendInterviewCancelledEmail(Interview interview, String reason) {
        String candidateName = fullName(interview.getJobApplication().getCandidate().getUser());
        String jobTitle      = interview.getJobApplication().getJob().getTitle();

        String body = String.format("""
                Dear %s,

                We regret to inform you that your interview for %s has been cancelled.

                Reason: %s

                Our team will be in touch to reschedule if applicable.

                Best regards,
                The RecruitPro Team
                """, candidateName, jobTitle,
                reason != null ? reason : "N/A");

        send(interview.getJobApplication().getCandidate().getUser().getEmail(), candidateName,
             String.format(AppConstants.EMAIL_SUBJECT_INTERVIEW_CANCELLED, jobTitle),
             body, NotificationType.INTERVIEW_CANCELLED,
             interview.getId(), AppConstants.REF_TYPE_INTERVIEW);
    }

    // ── Password reset ────────────────────────────────────────────────────────

    @Async
    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        String body = String.format("""
                Dear %s,

                We received a request to reset your RecruitPro password.

                Use the following token to reset your password (valid for %d minutes):

                  Token: %s

                If you did not request a password reset, please ignore this email.

                Best regards,
                The RecruitPro Team
                """, fullName(user),
                AppConstants.PASSWORD_RESET_TOKEN_EXPIRY_MINUTES,
                resetToken);

        send(user.getEmail(), fullName(user),
             AppConstants.EMAIL_SUBJECT_PASSWORD_RESET,
             body, NotificationType.PASSWORD_RESET,
             user.getId(), "USER");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void send(String toEmail, String toName, String subject,
                      String body, NotificationType type,
                      Long referenceId, String referenceType) {

        EmailLog log = EmailLog.builder()
                .recipientEmail(toEmail)
                .recipientName(toName)
                .subject(subject)
                .body(body)
                .emailType(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.setStatus(EmailStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            this.log.info("Email sent to {}: {}", toEmail, subject);
        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            this.log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        } finally {
            emailLogRepository.save(log);
        }
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
