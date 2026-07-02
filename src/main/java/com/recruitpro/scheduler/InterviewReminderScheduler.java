package com.recruitpro.scheduler;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.entity.Interview;
import com.recruitpro.repository.InterviewRepository;
import com.recruitpro.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewReminderScheduler {

    private final InterviewRepository interviewRepository;
    private final EmailService        emailService;

    /**
     * Runs every hour on the hour.
     * Finds all SCHEDULED interviews whose scheduled time falls within the next
     * [INTERVIEW_REMINDER_HOURS_BEFORE, INTERVIEW_REMINDER_HOURS_BEFORE + 1] hour window
     * and for which a reminder has not yet been sent, then sends the reminder email
     * and marks reminderSent = true to prevent duplicates.
     */
    @Scheduled(cron = "0 0 * * * *")   // every hour at :00
    @Transactional
    public void sendInterviewReminders() {
        LocalDateTime from = LocalDateTime.now()
                .plusHours(AppConstants.INTERVIEW_REMINDER_HOURS_BEFORE);
        LocalDateTime to   = from.plusHours(1);

        List<Interview> upcoming = interviewRepository
                .findScheduledBetweenWithNoReminder(from, to);

        if (upcoming.isEmpty()) {
            log.debug("InterviewReminderScheduler: no reminders to send");
            return;
        }

        log.info("InterviewReminderScheduler: sending {} reminder(s)", upcoming.size());

        for (Interview interview : upcoming) {
            try {
                emailService.sendInterviewReminderEmail(interview);
                interview.setReminderSent(true);
                interviewRepository.save(interview);
                log.info("Reminder sent for interview id={}, candidate={}",
                        interview.getId(),
                        interview.getJobApplication().getCandidate().getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send reminder for interview id={}: {}",
                        interview.getId(), e.getMessage());
            }
        }
    }
}
