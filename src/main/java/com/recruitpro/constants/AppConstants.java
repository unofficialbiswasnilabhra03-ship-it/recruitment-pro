package com.recruitpro.constants;

public final class AppConstants {

    private AppConstants() {}

    // ── Pagination defaults ───────────────────────────────────────────────────
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE    = 10;
    public static final int MAX_PAGE_SIZE        = 100;
    public static final String DEFAULT_SORT_BY   = "createdAt";
    public static final String DEFAULT_SORT_DIR  = "desc";

    // ── JWT ───────────────────────────────────────────────────────────────────
    public static final String TOKEN_PREFIX      = "Bearer ";
    public static final String AUTH_HEADER       = "Authorization";

    // ── Roles ─────────────────────────────────────────────────────────────────
    public static final String ROLE_ADMIN        = "ROLE_ADMIN";
    public static final String ROLE_HR           = "ROLE_HR";
    public static final String ROLE_INTERVIEWER  = "ROLE_INTERVIEWER";
    public static final String ROLE_CANDIDATE    = "ROLE_CANDIDATE";

    // ── File upload ───────────────────────────────────────────────────────────
    public static final long   MAX_RESUME_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final String ALLOWED_RESUME_TYPE_PDF  = "application/pdf";
    public static final String ALLOWED_RESUME_TYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    // ── Password reset ────────────────────────────────────────────────────────
    public static final long PASSWORD_RESET_TOKEN_EXPIRY_MINUTES = 30;

    // ── Email subjects ────────────────────────────────────────────────────────
    public static final String EMAIL_SUBJECT_APPLICATION_RECEIVED  = "Application Received – %s";
    public static final String EMAIL_SUBJECT_SHORTLISTED           = "You've Been Shortlisted – %s";
    public static final String EMAIL_SUBJECT_INTERVIEW_INVITATION  = "Interview Invitation – %s";
    public static final String EMAIL_SUBJECT_INTERVIEW_REMINDER    = "Interview Reminder – %s";
    public static final String EMAIL_SUBJECT_INTERVIEW_RESCHEDULED = "Interview Rescheduled – %s";
    public static final String EMAIL_SUBJECT_INTERVIEW_CANCELLED   = "Interview Cancelled – %s";
    public static final String EMAIL_SUBJECT_OFFER_LETTER          = "Offer Letter – %s";
    public static final String EMAIL_SUBJECT_REJECTION             = "Application Status Update – %s";
    public static final String EMAIL_SUBJECT_PASSWORD_RESET        = "Reset Your RecruitPro Password";

    // ── Notification reference types ──────────────────────────────────────────
    public static final String REF_TYPE_JOB_APPLICATION = "JOB_APPLICATION";
    public static final String REF_TYPE_INTERVIEW       = "INTERVIEW";
    public static final String REF_TYPE_JOB             = "JOB";

    // ── Scheduler ─────────────────────────────────────────────────────────────
    /** Send reminder this many hours before the interview */
    public static final int INTERVIEW_REMINDER_HOURS_BEFORE = 24;
}
