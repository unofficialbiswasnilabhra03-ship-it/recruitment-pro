package com.recruitpro.service.impl;

import com.recruitpro.dto.request.JobRequest;
import com.recruitpro.dto.response.JobResponse;
import com.recruitpro.entity.Company;
import com.recruitpro.entity.Job;
import com.recruitpro.entity.User;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobStatus;
import com.recruitpro.enums.JobType;
import com.recruitpro.exception.BadRequestException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.exception.UnauthorizedException;
import com.recruitpro.repository.CompanyRepository;
import com.recruitpro.repository.JobApplicationRepository;
import com.recruitpro.repository.JobRepository;
import com.recruitpro.repository.UserRepository;
import com.recruitpro.service.interfaces.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository         jobRepository;
    private final CompanyRepository     companyRepository;
    private final UserRepository        userRepository;
    private final JobApplicationRepository applicationRepository;

    @Override
    @Transactional
    public JobResponse createJob(Long currentUserId, JobRequest request) {
        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
        User postedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .responsibilities(request.getResponsibilities())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .status(JobStatus.OPEN)
                .experienceLevel(request.getExperienceLevel())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .openings(request.getOpenings() != null ? request.getOpenings() : 1)
                .applicationDeadline(request.getApplicationDeadline())
                .company(company)
                .postedBy(postedBy)
                .build();

        return toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long id, Long currentUserId, JobRequest request) {
        Job job = findActive(id);
        if (job.getDeletedAt() != null) {
            throw new BadRequestException("Cannot update a deleted job");
        }

        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setResponsibilities(request.getResponsibilities());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setOpenings(request.getOpenings());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setCompany(company);

        return toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getAllOpenJobs(Pageable pageable) {
        return jobRepository.findByStatus(JobStatus.OPEN, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByCompany(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchOpen(keyword, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> filterJobs(JobType jobType, ExperienceLevel expLevel,
                                         String location, Long companyId,
                                         BigDecimal salaryMin, BigDecimal salaryMax,
                                         String keyword, Pageable pageable) {
        return jobRepository.filterJobs(jobType, expLevel, location, companyId,
                salaryMin, salaryMax, keyword, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public JobResponse closeJob(Long id, Long currentUserId) {
        Job job = findActive(id);
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Job is already closed");
        }
        job.setStatus(JobStatus.CLOSED);
        return toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(Long id, Long currentUserId) {
        Job job = findActive(id);
        jobRepository.delete(job); // triggers @SQLDelete soft-delete
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Job findActive(Long id) {
        return jobRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
    }

    private JobResponse toResponse(Job j) {
        long appCount = applicationRepository.countByJobId(j.getId());
        return JobResponse.builder()
                .id(j.getId())
                .title(j.getTitle())
                .description(j.getDescription())
                .requirements(j.getRequirements())
                .responsibilities(j.getResponsibilities())
                .location(j.getLocation())
                .jobType(j.getJobType())
                .status(j.getStatus())
                .experienceLevel(j.getExperienceLevel())
                .salaryMin(j.getSalaryMin())
                .salaryMax(j.getSalaryMax())
                .openings(j.getOpenings())
                .applicationDeadline(j.getApplicationDeadline())
                .companyId(j.getCompany().getId())
                .companyName(j.getCompany().getName())
                .companyLogoUrl(j.getCompany().getLogoUrl())
                .postedByUserId(j.getPostedBy() != null ? j.getPostedBy().getId() : null)
                .postedByName(j.getPostedBy() != null
                        ? j.getPostedBy().getFirstName() + " " + j.getPostedBy().getLastName() : null)
                .applicationCount(appCount)
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}
