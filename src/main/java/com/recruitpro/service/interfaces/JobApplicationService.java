package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.ApplicationStatusUpdateRequest;
import com.recruitpro.dto.request.JobApplicationRequest;
import com.recruitpro.dto.response.JobApplicationResponse;
import com.recruitpro.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobApplicationService {

    // Candidate actions
    JobApplicationResponse apply(Long userId, JobApplicationRequest request);

    void withdraw(Long userId, Long applicationId);

    Page<JobApplicationResponse> getMyApplications(Long userId, Pageable pageable);

    JobApplicationResponse getMyApplication(Long userId, Long applicationId);

    // HR actions
    Page<JobApplicationResponse> getApplicationsByJob(Long jobId, ApplicationStatus status, Pageable pageable);

    Page<JobApplicationResponse> getApplicationsByCompany(Long companyId, ApplicationStatus status, Pageable pageable);

    JobApplicationResponse getApplicationById(Long applicationId);

    JobApplicationResponse updateStatus(Long applicationId, ApplicationStatusUpdateRequest request);

    JobApplicationResponse shortlist(Long applicationId);

    // Shared
    Page<JobApplicationResponse> getApplicationsByCandidate(Long candidateId, Pageable pageable);
}
