package com.recruitpro.dto.response;

import com.recruitpro.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobApplicationResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String companyLogoUrl;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private ApplicationStatus status;
    private String coverLetter;

    /**
     * hrNotes and rejectionReason are only populated when the authenticated
     * caller has ROLE_HR or ROLE_ADMIN. The service layer handles this
     * by projecting null for ROLE_CANDIDATE responses.
     */
    private String hrNotes;
    private String rejectionReason;

    private boolean hasResume;
    private String resumeDownloadUrl;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
