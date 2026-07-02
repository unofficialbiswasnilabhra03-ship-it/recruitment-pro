package com.recruitpro.controller;

import com.recruitpro.dto.request.JobRequest;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.JobResponse;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobType;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.JobService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting lifecycle: create, search, filter, close, delete")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Create a new job posting (HR / Admin)")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.success("Job created",
                        jobService.createJob(principal.getId(), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Update a job posting (HR / Admin)")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job updated",
                jobService.updateJob(id, principal.getId(), request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID (public)")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job fetched",
                jobService.getJobById(id)));
    }

    @GetMapping
    @Operation(summary = "List / search / filter open jobs (public)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> listJobs(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "10")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir,
            @RequestParam(required = false)            String search,
            @RequestParam(required = false)            JobType jobType,
            @RequestParam(required = false)            ExperienceLevel experienceLevel,
            @RequestParam(required = false)            String location,
            @RequestParam(required = false)            Long companyId,
            @RequestParam(required = false)            BigDecimal salaryMin,
            @RequestParam(required = false)            BigDecimal salaryMax) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        boolean hasFilters = jobType != null || experienceLevel != null
                || location != null || companyId != null
                || salaryMin != null || salaryMax != null;

        Page<JobResponse> result;
        if (hasFilters || search != null) {
            result = jobService.filterJobs(jobType, experienceLevel, location,
                    companyId, salaryMin, salaryMax, search, pageable);
        } else {
            result = jobService.getAllOpenJobs(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", result));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get all jobs for a company (public)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched",
                jobService.getJobsByCompany(companyId, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Close a job posting (HR / Admin)")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Job closed",
                jobService.closeJob(id, principal.getId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Soft-delete a job posting (HR / Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        jobService.deleteJob(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Job deleted"));
    }
}
