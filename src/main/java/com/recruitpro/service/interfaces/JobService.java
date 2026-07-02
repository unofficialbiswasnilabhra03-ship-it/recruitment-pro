package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.JobRequest;
import com.recruitpro.dto.response.JobResponse;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface JobService {

    JobResponse createJob(Long currentUserId, JobRequest request);

    JobResponse updateJob(Long id, Long currentUserId, JobRequest request);

    JobResponse getJobById(Long id);

    Page<JobResponse> getAllOpenJobs(Pageable pageable);

    Page<JobResponse> getJobsByCompany(Long companyId, Pageable pageable);

    Page<JobResponse> searchJobs(String keyword, Pageable pageable);

    Page<JobResponse> filterJobs(JobType jobType, ExperienceLevel expLevel,
                                  String location, Long companyId,
                                  BigDecimal salaryMin, BigDecimal salaryMax,
                                  String keyword, Pageable pageable);

    JobResponse closeJob(Long id, Long currentUserId);

    void deleteJob(Long id, Long currentUserId);
}
