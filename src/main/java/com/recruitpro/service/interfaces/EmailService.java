package com.recruitpro.service.interfaces;

import com.recruitpro.entity.Interview;
import com.recruitpro.entity.JobApplication;
import com.recruitpro.entity.User;

public interface EmailService {

    void sendApplicationReceivedEmail(JobApplication application);

    void sendShortlistEmail(JobApplication application);

    void sendRejectionEmail(JobApplication application);

    void sendInterviewInvitationEmail(Interview interview);

    void sendInterviewReminderEmail(Interview interview);

    void sendInterviewRescheduledEmail(Interview interview, String reason);

    void sendInterviewCancelledEmail(Interview interview, String reason);

    void sendOfferLetterEmail(JobApplication application);

    void sendPasswordResetEmail(User user, String resetToken);
}
